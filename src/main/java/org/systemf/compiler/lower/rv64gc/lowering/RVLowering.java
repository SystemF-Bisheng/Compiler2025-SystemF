package org.systemf.compiler.lower.rv64gc.lowering;

import org.systemf.compiler.analysis.CFGAnalysisResult;
import org.systemf.compiler.analysis.DominanceAnalysisResult;
import org.systemf.compiler.analysis.FrequencyAnalysisResult;
import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.ExternalFunction;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.type.*;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.Void;
import org.systemf.compiler.ir.type.interfaces.Indexable;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Parameter;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.*;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyIntBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyUnary;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.Call;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.CallVoid;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Alloca;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.GetPtr;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.ir.value.instruction.terminal.Ret;
import org.systemf.compiler.ir.value.instruction.terminal.RetVoid;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.lower.rv64gc.instruction.*;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.lower.rv64gc.module.RVStackState;
import org.systemf.compiler.optimization.OptimizedResult;
import org.systemf.compiler.optimization.pass.util.CodeMotionHelper;
import org.systemf.compiler.query.EntityProvider;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.TriFunction;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.BiFunction;

public enum RVLowering implements EntityProvider<RVLoweringResult> {
	INSTANCE;

	@Override
	public RVLoweringResult produce() {
		var query = QueryManager.getInstance();
		var optimized = query.get(OptimizedResult.class);
		var module = optimized.module();
		query.invalidate(optimized);

		var res = new RVLoweringContext(module).run();

		query.invalidateAllAttributes(module);
		module.getFunctions().values().forEach(query::invalidateAllAttributes);
		return res;
	}

	private static class RVLoweringContext extends InstructionVisitorBase<Void> {
		private final QueryManager query = QueryManager.getInstance();
		private final Module oldModule;
		private final Module newModule = new Module();
		private final HashMap<BasicBlock, Integer> frequency = new HashMap<>();
		private final HashMap<Function, Function> newFunctions = new HashMap<>();
		private final HashMap<BasicBlock, BasicBlock> newBlocks = new HashMap<>();
		private final HashMap<Value, Value> substitute = new HashMap<>();
		private final HashMap<Function, RVStackState> stacks = new HashMap<>();
		private CFGAnalysisResult oldCFG;
		private FrequencyAnalysisResult oldFrequency;
		private Function curFunction;
		private BasicBlock curBlock;
		private RVStackState curStack;

		private ExternalFunction memZero;

		public RVLoweringContext(Module oldModule) {
			this.oldModule = oldModule;
		}

		private void translateBlock(BasicBlock oldBlock, BasicBlock newBlock) {
			curBlock = newBlock;
			for (var inst : oldBlock.instructions) inst.accept(this);
		}

		private RVParallelMove genParallelMove(BasicBlock oldPred, BasicBlock oldBlock, BasicBlock newBlock) {
			var newPred = substituted(oldPred);
			var oldPredSuccs = oldCFG.successors(oldPred);
			var newMove = new RVParallelMove();
			if (oldPredSuccs.size() == 1) CodeMotionHelper.insertTail(newPred, newMove);
			else {
				var newInter = new BasicBlock(newName("phiTmp"));
				curFunction.insertBlock(newInter);
				newInter.insertInstruction(newMove);
				newInter.insertInstruction(new Br(newBlock));
				newPred.getTerminator().replaceAll(newBlock, newInter);

				frequency.put(newInter, oldFrequency.distribute(oldPred, oldBlock));
			}
			return newMove;
		}

		private void translatePhi(BasicBlock oldBlock) {
			if (!(oldBlock.getFirstInstruction() instanceof Phi)) return;
			var newBlock = substituted(oldBlock);
			var oldPreds = oldCFG.predecessors(oldBlock);

			var parallels = new HashMap<BasicBlock, RVParallelMove>();
			oldBlock.instructions.stream().takeWhile(inst -> inst instanceof Phi).map(inst -> (Phi) inst)
					.forEach(oldPhi -> {
						var newHolder = substituted(oldPhi);
						for (var oldPred : oldPreds) {
							var newValue = substituted(oldPhi.getIncoming().get(oldPred));
							if (newValue instanceof Undefined) continue;
							parallels.computeIfAbsent(oldPred, _ -> genParallelMove(oldPred, oldBlock, newBlock))
									.addMove(newHolder, newValue);
						}
					});
		}

		private void translateFunction(Function oldFunction, Function newFunction) {
			curFunction = newFunction;
			curStack = stacks.get(newFunction);
			oldCFG = query.getAttribute(oldFunction, CFGAnalysisResult.class);
			oldFrequency = query.getAttribute(oldFunction, FrequencyAnalysisResult.class);

			var oldDom = query.getAttribute(oldFunction, DominanceAnalysisResult.class).dominance();
			oldFunction.getBlocks().forEach(oldBlock -> {
				var newBlock = new BasicBlock(newName(oldBlock.getName()));
				newFunction.insertBlock(newBlock);
				newBlocks.put(oldBlock, newBlock);
				frequency.put(newBlock, oldFrequency.frequency(oldBlock));
			});
			oldFunction.getBlocks().stream().sorted(Comparator.comparingInt(oldDom::getDfn))
					.forEach(oldBlock -> translateBlock(oldBlock, substituted(oldBlock)));
			oldFunction.getBlocks().forEach(this::translatePhi);
			newFunction.setEntryBlock(newBlocks.get(oldFunction.getEntryBlock()));
		}

		private void produceNewGlobal(GlobalVariable global) {
			var newGlobal = new GlobalVariable(global.getName(), global.valueType, global.getInitializer(),
					I64.INSTANCE);
			substitute.put(global, newGlobal);
			newModule.addGlobalVariable(newGlobal);
		}

		private void produceNewExternal(ExternalFunction external) {
			var newExternal = new ExternalFunction(external.getName(),
					RVTypeHelper.lowerFunctionType((FunctionType) external.getType()));
			substitute.put(external, newExternal);
			newModule.addExternalFunction(newExternal);
		}

		private Parameter produceNewParam(Parameter param) {
			return new Parameter(RVTypeHelper.lowerType(param.getType()), newName(param.getName()));
		}

		private void produceNewFunction(Function function) {
			var newParams = Arrays.stream(function.getFormalArgs()).map(oldParam -> {
				var newParam = produceNewParam(oldParam);
				substitute.put(oldParam, newParam);
				return newParam;
			}).toArray(Parameter[]::new);
			var newRet = RVTypeHelper.lowerType(function.getReturnType());
			var newFunc = new Function(function.getName(), newRet, newParams);
			substitute.put(function, newFunc);
			newFunctions.put(function, newFunc);
			newModule.addFunction(newFunc);
			stacks.put(newFunc, new RVStackState());
		}

		private void addIntrinsicFunctions() {
			memZero = new ExternalFunction("_rv64gc_memzero",
					new FunctionType(Void.INSTANCE, I64.INSTANCE, I64.INSTANCE));
			newModule.addExternalFunction(memZero);
		}

		private void translateModule() {
			oldModule.getGlobalDeclarations().values().forEach(this::produceNewGlobal);
			oldModule.getExternalFunctions().values().forEach(this::produceNewExternal);
			addIntrinsicFunctions();
			oldModule.getFunctions().values().forEach(this::produceNewFunction);
			newFunctions.forEach(this::translateFunction);
		}

		public RVLoweringResult run() {
			translateModule();
			oldModule.destroy();
			return new RVLoweringResult(new RVModule(newModule, frequency, stacks));
		}

		private void insertInstruction(Instruction instruction) {
			curBlock.insertInstruction(instruction);
		}

		private BasicBlock substituted(BasicBlock block) {
			return newBlocks.get(block);
		}

		private Value substituted(Value value) {
			if (value instanceof Constant) return value;
			var res = substitute.get(value);
			if (res == null) throw new RuntimeException("Cannot substitute value " + ValueUtil.dumpIdentifier(value));
			return res;
		}

		private String newName(String name) {
			return newModule.getNonConflictName(name);
		}

		@Override
		protected Void defaultValue() {
			throw new UnsupportedOperationException();
		}

		private Void handleBinary(DummyBinary inst, TriFunction<String, Value, Value, Instruction> getInst) {
			var name = newName(inst.getName());
			var x = substituted(inst.getX());
			var y = substituted(inst.getY());
			var newInst = getInst.apply(name, x, y);
			insertInstruction(newInst);
			substitute.put(inst, (Value) newInst);
			return null;
		}

		private Void handleIntBinary(DummyIntBinary inst, TriFunction<String, Value, Value, Instruction> on32,
				TriFunction<String, Value, Value, Instruction> on64) {
			var type = inst.getType();
			TriFunction<String, Value, Value, Instruction> getInst;
			if (type instanceof I32) getInst = on32;
			else if (type instanceof I64) getInst = on64;
			else throw new UnsupportedOperationException();

			return handleBinary(inst, getInst);
		}

		@Override
		public Void visit(Add inst) {
			return handleIntBinary(inst, RVAddWord::new, RVAdd::new);
		}

		@Override
		public Void visit(Sub inst) {
			return handleIntBinary(inst, RVSubWord::new, RVSub::new);
		}

		@Override
		public Void visit(Mul inst) {
			return handleIntBinary(inst, RVMulWord::new, RVMul::new);
		}

		@Override
		public Void visit(SDiv inst) {
			return handleIntBinary(inst, RVDivWord::new, RVDiv::new);
		}

		@Override
		public Void visit(SRem inst) {
			return handleIntBinary(inst, RVRemWord::new, RVRem::new);
		}

		@Override
		public Void visit(ICmp inst) {
			var name = newName(inst.getName());
			var x = substituted(inst.getX());
			var y = substituted(inst.getY());
			Value res;
			switch (inst.method) {
				case EQ -> {
					var xLy = new RVSetLessThan(newName("eqXLY"), x, y);
					insertInstruction(xLy);
					var yLx = new RVSetLessThan(newName("eqYLX"), y, x);
					insertInstruction(yLx);
					var neq = new RVOr(newName("neqXY"), xLy, yLx);
					insertInstruction(neq);
					var eq = new RVXor(name, neq, ConstantInt64.valueOf(1));
					insertInstruction(eq);
					res = eq;
				}
				case NE -> {
					var xLy = new RVSetLessThan(newName("neqXLY"), x, y);
					insertInstruction(xLy);
					var yLx = new RVSetLessThan(newName("neqYLX"), y, x);
					insertInstruction(yLx);
					var neq = new RVOr(name, xLy, yLx);
					insertInstruction(neq);
					res = neq;
				}
				case LT -> {
					var xLy = new RVSetLessThan(name, x, y);
					insertInstruction(xLy);
					res = xLy;
				}
				case GE -> {
					var xLy = new RVSetLessThan(newName("geXLY"), x, y);
					insertInstruction(xLy);
					var xGEy = new RVXor(name, xLy, ConstantInt64.valueOf(1));
					insertInstruction(xGEy);
					res = xGEy;
				}
				default -> throw new UnsupportedOperationException();
			}
			substitute.put(inst, res);
			return null;
		}

		private Optional<FloatContractInfo> checkContract(Value a, Value b) {
			boolean neg = false;
			while (a instanceof FNeg aNeg) {
				neg = !neg;
				a = aNeg.getX();
			}
			if (!(a instanceof FMul aMul)) return Optional.empty();
			var x = aMul.getX();
			var y = aMul.getY();
			while (x instanceof FNeg xNeg) {
				neg = !neg;
				x = xNeg.getX();
			}
			while (y instanceof FNeg yNeg) {
				neg = !neg;
				y = yNeg.getX();
			}
			return Optional.of(new FloatContractInfo(substituted(x), substituted(y), substituted(b), neg));
		}

		@Override
		public Void visit(FAdd inst) {
			var name = newName(inst.getName());
			return checkContract(inst.getX(), inst.getY()).map(contract -> {
				Instruction newInst;
				if (contract.neg()) newInst = new RVFloatNegMulSub(name, contract.x, contract.y, contract.z);
				else newInst = new RVFloatMulAdd(name, contract.x, contract.y, contract.z);
				insertInstruction(newInst);
				substitute.put(inst, (Value) newInst);
				return (Void) null;
			}).orElseGet(() -> handleBinary(inst, RVFloatAdd::new));
		}

		@Override
		public Void visit(FSub inst) {
			var name = newName(inst.getName());
			return checkContract(inst.getX(), inst.getY()).map(contract -> {
				Instruction newInst;
				if (contract.neg()) newInst = new RVFloatNegMulAdd(name, contract.x, contract.y, contract.z);
				else newInst = new RVFloatMulSub(name, contract.x, contract.y, contract.z);
				insertInstruction(newInst);
				substitute.put(inst, (Value) newInst);
				return (Void) null;
			}).orElseGet(() -> handleBinary(inst, RVFloatSub::new));
		}

		@Override
		public Void visit(FMul inst) {
			return handleBinary(inst, RVFloatMul::new);
		}

		@Override
		public Void visit(FDiv inst) {
			return handleBinary(inst, RVFloatDiv::new);
		}

		@Override
		public Void visit(FNeg inst) {
			return handleUnary(inst, RVFloatNeg::new);
		}

		@Override
		public Void visit(FCmp inst) {
			var name = newName(inst.getName());
			var x = substituted(inst.getX());
			var y = substituted(inst.getY());
			Value res;
			switch (inst.method) {
				case EQ -> {
					var eq = new RVFloatEq(name, x, y);
					insertInstruction(eq);
					res = eq;
				}
				case NE -> {
					var eq = new RVFloatEq(newName("fneEq"), x, y);
					insertInstruction(eq);
					var neq = new RVXor(name, eq, ConstantInt64.valueOf(1));
					insertInstruction(neq);
					res = neq;
				}
				case LT -> {
					var lt = new RVFloatLt(name, x, y);
					insertInstruction(lt);
					res = lt;
				}
				case GE -> {
					var ge = new RVFloatLe(name, y, x);
					insertInstruction(ge);
					res = ge;
				}
				default -> throw new UnsupportedOperationException();
			}
			substitute.put(inst, res);
			return null;
		}

		@Override
		public Void visit(And inst) {
			return handleBinary(inst, RVAnd::new);
		}

		@Override
		public Void visit(Or inst) {
			return handleBinary(inst, RVOr::new);
		}

		@Override
		public Void visit(Xor inst) {
			return handleBinary(inst, RVXor::new);
		}

		@Override
		public Void visit(Shl inst) {
			return handleIntBinary(inst, RVShiftLeft::new, RVShiftLeftWord::new);
		}

		@Override
		public Void visit(AShr inst) {
			return handleIntBinary(inst, RVShiftRightArith::new, RVShiftRightArithWord::new);
		}

		@Override
		public Void visit(LShr inst) {
			return handleIntBinary(inst, RVShiftRightLogical::new, RVShiftRightLogicalWord::new);
		}

		private Void handleUnary(DummyUnary inst, BiFunction<String, Value, Instruction> getInst) {
			var name = newName(inst.getName());
			var x = substituted(inst.getX());
			var newInst = getInst.apply(name, x);
			insertInstruction(newInst);
			substitute.put(inst, (Value) newInst);
			return null;
		}

		@Override
		public Void visit(FpToSi32 inst) {
			return handleUnary(inst, RVCvtFloat2Word::new);
		}

		@Override
		public Void visit(PtrCast inst) {
			substitute.put(inst, substituted(inst.getX()));
			return null;
		}

		@Override
		public Void visit(Si32ToFp inst) {
			return handleUnary(inst, RVCvtWord2Float::new);
		}

		@Override
		public Void visit(Si32ToSi64 inst) {
			substitute.put(inst, substituted(inst.getX()));
			return null;
		}

		@Override
		public Void visit(Si64ToSi32 inst) {
			substitute.put(inst, substituted(inst.getX()));
			return null;
		}

		@Override
		public Void visit(Call inst) {
			var name = newName(inst.getName());
			var func = substituted(inst.getFunction());
			var args = Arrays.stream(inst.getArgs()).map(this::substituted).toArray(Value[]::new);
			var newInst = new Call(name, func, args);
			insertInstruction(newInst);
			substitute.put(inst, newInst);
			return null;
		}

		@Override
		public Void visit(CallVoid inst) {
			var func = substituted(inst.getFunction());
			var args = Arrays.stream(inst.getArgs()).map(this::substituted).toArray(Value[]::new);
			var newInst = new CallVoid(func, args);
			insertInstruction(newInst);
			return null;
		}

		@Override
		public Void visit(Alloca inst) {
			var name = newName(inst.getName());
			var type = inst.valueType;
			var pos = curStack.allocate(RVTypeHelper.sizeOf(type), RVTypeHelper.alignmentOf(type));
			var newInst = new RVAdd(name, curStack.fp, ConstantInt64.valueOf(pos));
			insertInstruction(newInst);
			substitute.put(inst, newInst);
			return null;
		}

		@Override
		public Void visit(Load inst) {
			var name = newName(inst.getName());
			var type = RVTypeHelper.lowerType(inst.getType());
			var ptr = substituted(inst.getPointer());
			var newInst = switch (type) {
				case I32 _ -> new RVLoadWord(name, ptr, 0);
				case I64 _ -> new RVLoadDWord(name, ptr, 0);
				case Float _ -> new RVLoadFloat(name, ptr, 0);
				case null, default -> throw new UnsupportedOperationException();
			};
			insertInstruction(newInst);
			substitute.put(inst, newInst);
			return null;
		}

		@Override
		public Void visit(GetPtr inst) {
			var name = newName(inst.getName());
			var orgPtr = inst.getArrayPtr();
			var ptr = substituted(orgPtr);
			var index = substituted(inst.getIndex());
			var step = RVTypeHelper.sizeOf(
					((Indexable) ((Pointer) orgPtr.getType()).getElementType()).getElementType());
			var advance = new RVMul(newName("gepMul"), index, ConstantInt64.valueOf(step));
			insertInstruction(advance);
			var res = new RVAdd(name, ptr, advance);
			insertInstruction(res);
			substitute.put(inst, res);
			return null;
		}

		private void fullStoreArray(ConstantArray srcArray, Value dest, long offset) {
			var inner = srcArray.getElementType();
			var length = srcArray.getSize();
			var step = RVTypeHelper.sizeOf(inner);
			for (int i = 0; i < length; ++i) {
				var content = srcArray.getContent(i);
				processStore(content, dest, inner, offset + step * i);
			}
		}

		private void zeroArray(long size, Value dest, long offset) {
			var offsetDest = new RVAdd(newName("storeAdd"), dest, ConstantInt64.valueOf(offset));
			insertInstruction(offsetDest);
			insertInstruction(new CallVoid(memZero, offsetDest, ConstantInt64.valueOf(size)));
		}

		private long calcSparseSize(Constant constant) {
			var type = constant.getType();
			return switch (type) {
				case I32 _, I64 _ -> ValueUtil.getConstantInt(constant) == 0 ? RVTypeHelper.sizeOf(type) : 0;
				case Float _ -> ValueUtil.getConstantFloat(constant) == 0 ? RVTypeHelper.sizeOf(type) : 0;
				case Array _ -> {
					var constArr = (ConstantArray) constant;
					if (constArr instanceof ArrayZeroInitializer) yield RVTypeHelper.sizeOf(type);
					var length = constArr.getSize();
					long res = 0;
					for (int i = 0; i < length; ++i) res += calcSparseSize(constArr.getContent(i));
					yield res;
				}
				case null, default -> throw new UnsupportedOperationException();
			};
		}

		private void sparseStore(Constant src, Value dest, long offset) {
			var type = src.getType();
			if (calcSparseSize(src) == RVTypeHelper.sizeOf(type)) return;
			if (src instanceof ConstantArray srcArray) {
				var inner = srcArray.getElementType();
				var length = srcArray.getSize();
				var step = RVTypeHelper.sizeOf(inner);
				for (int i = 0; i < length; ++i) {
					var content = srcArray.getContent(i);
					sparseStore(content, dest, offset + step * i);
				}
			} else processStore(src, dest, type, offset);
		}

		private double calcSparseRate(ConstantArray srcArray) {
			return (double) calcSparseSize(srcArray) / RVTypeHelper.sizeOf(srcArray.getType());
		}

		private void storeArray(ConstantArray srcArray, Value dest, long offset) {
			if (calcSparseRate(srcArray) >= 0.7) {
				zeroArray(RVTypeHelper.sizeOf(srcArray.getType()), dest, offset);
				sparseStore(srcArray, dest, offset);
			} else fullStoreArray(srcArray, dest, offset);
		}

		private void processStore(Value src, Value dest, Type type, long offset) {
			if (type instanceof Array) storeArray((ConstantArray) src, dest, offset);
			else {
				var newInst = switch (type) {
					case I32 _ -> new RVStoreWord(src, dest, offset);
					case I64 _ -> new RVStoreDWord(src, dest, offset);
					case Float _ -> new RVStoreFloat(src, dest, offset);
					case null, default -> throw new UnsupportedOperationException();
				};
				insertInstruction(newInst);
			}
		}

		@Override
		public Void visit(Store inst) {
			var orgSrc = inst.getSrc();
			var src = substituted(orgSrc);
			var type = RVTypeHelper.lowerType(orgSrc.getType());
			var dest = substituted(inst.getDest());
			processStore(src, dest, type, 0);
			return null;
		}

		@Override
		public Void visit(Phi inst) {
			var name = newName(inst.getName());
			var newInst = new RVRegPlaceholder(RVTypeHelper.lowerType(inst.getType()), name);
			insertInstruction(newInst);
			substitute.put(inst, newInst);
			return null;
		}

		@Override
		public Void visit(Br inst) {
			insertInstruction(new Br(substituted(inst.getTarget())));
			return null;
		}

		@Override
		public Void visit(CondBr inst) {
			var orgCond = inst.getCondition();
			var trueTarget = substituted(inst.getTrueTarget());
			var falseTarget = substituted(inst.getFalseTarget());
			Instruction newInst;
			if (orgCond instanceof ICmp iCmp) {
				var x = substituted(iCmp.getX());
				var y = substituted(iCmp.getY());
				newInst = switch (iCmp.method) {
					case EQ -> new RVBranchEq(x, y, trueTarget, falseTarget);
					case NE -> new RVBranchEq(x, y, falseTarget, trueTarget);
					case LT -> new RVBranchLess(x, y, trueTarget, falseTarget);
					case GE -> new RVBranchLess(x, y, falseTarget, trueTarget);
					default -> throw new UnsupportedOperationException();
				};
			} else {
				var cond = substituted(orgCond);
				newInst = new RVBranchEq(cond, ConstantInt64.valueOf(0), falseTarget, trueTarget);
			}
			insertInstruction(newInst);
			return null;
		}

		@Override
		public Void visit(Ret inst) {
			insertInstruction(new Ret(substituted(inst.getReturnValue())));
			return null;
		}

		@Override
		public Void visit(RetVoid inst) {
			insertInstruction(RetVoid.INSTANCE);
			return null;
		}

		private record FloatContractInfo(Value x, Value y, Value z, boolean neg) {
		}
	}
}
