package org.systemf.compiler.machine.rv64gc;

import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.ExternalFunction;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.global.IFunction;
import org.systemf.compiler.ir.type.Void;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.*;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyTriple;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyUnary;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.Call;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.CallVoid;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.ir.value.instruction.terminal.Ret;
import org.systemf.compiler.ir.value.instruction.terminal.RetVoid;
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;
import org.systemf.compiler.lower.rv64gc.allocate.RVAllocatedResult;
import org.systemf.compiler.lower.rv64gc.analysis.RVLiveRangeAnalysisResult;
import org.systemf.compiler.lower.rv64gc.analysis.RVRegUsageAnalysisResult;
import org.systemf.compiler.lower.rv64gc.instruction.*;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.lower.rv64gc.module.position.RVRegister;
import org.systemf.compiler.lower.rv64gc.module.stack.RVStackState;
import org.systemf.compiler.lower.rv64gc.util.RVRegUtil;
import org.systemf.compiler.lower.rv64gc.util.RVTypeHelper;
import org.systemf.compiler.query.EntityProvider;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.MathUtil;

import java.util.*;
import java.util.stream.Collectors;

public enum RVGenerator implements EntityProvider<RVMachineCodeResult> {
	INSTANCE;

	@Override
	public RVMachineCodeResult produce() {
		var query = QueryManager.getInstance();
		var allocated = query.get(RVAllocatedResult.class);
		var rvModule = allocated.module();

		var res = new RVGeneratorContext(rvModule).run();

		query.invalidate(allocated);
		query.invalidateAllAttributes(rvModule.module());
		rvModule.module().getFunctions().values().forEach(query::invalidateAllAttributes);
		return res;
	}

	private static class RVGeneratorContext {
		private final RVModule rvModule;
		private final Module module;
		private final RVAsmCode result = new RVAsmCode();

		public RVGeneratorContext(RVModule rvModule) {
			this.rvModule = rvModule;
			this.module = rvModule.module();
		}

		private void genContent(Constant initializer) {
			switch (initializer) {
				case ConstantInt32 constI32 -> result.addLine(String.format(".4byte %#X", (int) constI32.value));
				case ConstantInt64 constI64 -> result.addLine(String.format(".8byte %#X", constI64.value));
				case ConstantFloat constFloat -> result.addLine(
						String.format(".4byte %#X", java.lang.Float.floatToIntBits((float) constFloat.value)));
				case ConstantArray constArray -> {
					var len = constArray.getSize();
					for (int i = 0; i < len; i++) genContent(constArray.getContent(i));
				}
				case null, default -> throw new UnsupportedOperationException();
			}
		}

		private void genInitializer(Constant initializer) {
			genContent(initializer);
		}

		private void genGlobalVar(String name, GlobalVariable global) {
			var initializer = global.getInitializer();
			var size = RVTypeHelper.sizeOf(global.valueType);
			var alignment = RVTypeHelper.alignmentOf(global.valueType);
			if (initializer instanceof ArrayZeroInitializer) {
				result.addLine(".bss");
				result.addLine(String.format(".balign %d", alignment));
				result.addLine(String.format(".global %s", name));
				result.addLine(String.format("%s:", name));
				result.addLine(String.format(".zero %d", size));
			} else {
				result.addLine(".data");
				result.addLine(String.format(".balign %d", alignment));
				result.addLine(String.format(".global %s", name));
				result.addLine(String.format("%s:", name));
				genInitializer(initializer);
			}
		}

		private void genGlobalVar() {
			module.getGlobalDeclarations().forEach(this::genGlobalVar);
		}

		public void genIntrinsicFunction(String name) {
			switch (name) {
				case "_rv64gc_memzero" -> result.add("""
						.text
						.balign 16
						_rv64gc_memzero:
						mv a2, a1
						li a1, 0
						tail memset
						
						""");
				case null, default -> throw new UnsupportedOperationException();
			}
		}

		public void genExternalFunction(String name, ExternalFunction externalFunction) {
			if (name.startsWith("_rv64gc")) genIntrinsicFunction(name);
			// Do nothing otherwise since all symbols are implicitly external
		}

		private void genExternalFunction() {
			module.getExternalFunctions().forEach(this::genExternalFunction);
		}

		private void genFunction(String name, Function function) {
			result.addLine(".text");
			result.addLine(".balign 16");
			result.addLine(String.format(".global %s", name));
			result.addLine(String.format("%s:", name));
			new RVGenFunctionContext(rvModule, function, result).run();
		}

		private void genFunction() {
			module.getFunctions().forEach(this::genFunction);
		}

		public RVMachineCodeResult run() {
			genGlobalVar();
			genExternalFunction();
			genFunction();
			return new RVMachineCodeResult(result.toString());
		}
	}

	private static class RVGenFunctionContext extends InstructionVisitorBase<Void> {
		private final RVModule rvModule;
		private final List<RVRegister> regSaved;
		private final Function function;
		private final RVAsmCode result;
		private final Set<BasicBlock> generatedBlock = new HashSet<>();
		private final RVStackState stackFrame;
		private final RVCacheManager cacheManager = new RVCacheManager();
		private final RVLiveRangeAnalysisResult liveRange;
		private final String epilogueName;
		private long stackSize = 0;

		public RVGenFunctionContext(RVModule rvModule, Function function, RVAsmCode result) {
			this.rvModule = rvModule;

			var hasCall = function.allInstructions().anyMatch(inst -> inst instanceof AbstractCall);
			var query = QueryManager.getInstance();
			this.regSaved = query.getAttribute(rvModule, RVRegUsageAnalysisResult.class).usage(function).stream()
					.filter(RVRegUtil::isSaved).collect(Collectors.toCollection(ArrayList::new));
			this.regSaved.add(RVRegUtil.FRAME_POINTER);
			if (hasCall) this.regSaved.add(RVRegUtil.RETURN_ADDRESS);

			this.function = function;
			this.result = result;
			this.stackFrame = rvModule.stacks().get(function);

			this.liveRange = query.getAttribute(function, RVLiveRangeAnalysisResult.class);

			this.epilogueName = rvModule.module().getNonConflictName(function.getName() + "Epilogue");
		}

		private void genPrologue() {
			stackSize += RVGenerateHelper.backupRegisters(regSaved, result);

			stackSize += stackFrame.getSize();
			stackSize = MathUtil.roundTo(stackSize, RVRegUtil.DEFAULT_STACK_ALIGNMENT);
			RVGenerateHelper.subtractSp(RVRegUtil.FRAME_POINTER, stackSize, result);

			RVGenerateHelper.loadArgs(rvModule, function.getFormalArgs(), cacheManager, result);

			RVGenerateHelper.moveRegister(RVRegUtil.STACK_POINTER, RVRegUtil.FRAME_POINTER, result);

			cacheManager.invalidateAll(result);
		}

		private void genEpilogue() {
			result.addLine(String.format("%s:", epilogueName));
			RVGenerateHelper.addSp(stackSize, result);

			RVGenerateHelper.restoreRegisters(regSaved, result);

			result.addLine("ret");
		}

		private void handleBranch(RVCompBranch inst, String trueOperator, String falseOperator) {
			var trueTarget = inst.getTrueTarget();
			var falseTarget = inst.getFalseTarget();
			int leftCount = 2;
			if (generatedBlock.contains(trueTarget)) --leftCount;
			if (generatedBlock.contains(falseTarget)) --leftCount;

			var regX = loadRegister(inst.getX());
			var regY = loadRegister(inst.getY());
			cacheManager.unlock(regX);
			cacheManager.unlock(regY);
			cacheManager.invalidateAll(result);

			if (leftCount == 0) {
				var trueFreq = rvModule.frequency().get(trueTarget);
				var falseFreq = rvModule.frequency().get(falseTarget);
				if (trueFreq < falseFreq) {
					result.addLine(String.format("%s %s, %s, %s", falseOperator, regX, regY, falseTarget.getName()));
					result.addLine(String.format("j %s", trueTarget.getName()));
				} else {
					result.addLine(String.format("%s %s, %s, %s", trueOperator, regX, regY, trueTarget.getName()));
					result.addLine(String.format("j %s", falseTarget.getName()));
				}
			} else if (leftCount == 2) {
				var trueFreq = rvModule.frequency().get(trueTarget);
				var falseFreq = rvModule.frequency().get(falseTarget);
				if (trueFreq < falseFreq) {
					result.addLine(String.format("%s %s, %s, %s", trueOperator, regX, regY, trueTarget.getName()));
					genBlock(falseTarget);
					genBlock(trueTarget);
				} else {
					result.addLine(String.format("%s %s, %s, %s", falseOperator, regX, regY, falseTarget.getName()));
					genBlock(trueTarget);
					genBlock(falseTarget);
				}
			} else if (generatedBlock.contains(trueTarget)) {
				result.addLine(String.format("%s %s, %s, %s", trueOperator, regX, regY, trueTarget.getName()));
				genBlock(falseTarget);
			} else {
				result.addLine(String.format("%s %s, %s, %s", falseOperator, regX, regY, falseTarget.getName()));
				genBlock(trueTarget);
			}
		}

		private void genBlock(BasicBlock block) {
			if (!generatedBlock.add(block)) return;
			result.addLine(String.format("%s:", block.getName()));
			for (var inst : block.instructions)
				if (!(inst instanceof Terminal)) {
					filterAliveBefore(inst);
					inst.accept(this);
				}
			cacheManager.unlockAll();
			var term = block.getTerminator();
			switch (term) {
				case Br br -> {
					cacheManager.invalidateAll(result);
					var target = br.getTarget();
					if (generatedBlock.contains(target)) result.addLine(String.format("j %s", target.getName()));
					else genBlock(target);
				}
				case Ret ret -> {
					RVGenerateHelper.putReturn(
							Objects.requireNonNull(RVGenerateHelper.typedPositionOf(rvModule, ret.getReturnValue())),
							cacheManager, result);
					cacheManager.invalidateAll(result);
					result.addLine(String.format("j %s", epilogueName));
				}
				case RetVoid _ -> {
					cacheManager.invalidateAll(result);
					result.addLine(String.format("j %s", epilogueName));
				}
				case RVBranchEq branchEq -> handleBranch(branchEq, "beq", "bne");
				case RVBranchLess branchLess -> handleBranch(branchLess, "blt", "bge");
				case null, default -> throw new UnsupportedOperationException();
			}
		}

		public void run() {
			genPrologue();
			genBlock(function.getEntryBlock());
			genEpilogue();
		}

		private void filterAliveBefore(Instruction inst) {
			cacheManager.filter(
					liveRange.aliveBefore(inst).stream().map(val -> RVGenerateHelper.typedPositionOf(rvModule, val))
							.collect(Collectors.toSet()));
		}

		private void filterAliveAfter(Instruction inst) {
			cacheManager.filter(
					liveRange.aliveAfter(inst).stream().map(val -> RVGenerateHelper.typedPositionOf(rvModule, val))
							.collect(Collectors.toSet()));
		}

		@Override
		protected Void defaultValue() {
			throw new UnsupportedOperationException();
		}

		private Void handleCall(AbstractCall inst, RVTypedPosition ret) {
			var aliveBefore = liveRange.aliveBefore(inst);
			var aliveAfter = liveRange.aliveAfter(inst);
			var toSave = aliveBefore.stream().filter(aliveAfter::contains)
					.map(value -> RVGenerateHelper.positionOf(rvModule, value)).filter(pos -> pos instanceof RVRegister)
					.map(pos -> (RVRegister) pos).filter(RVRegUtil::isNonSaved).toList();

			var args = inst.getArgs();
			var size = RVGenerateHelper.backupRegisters(toSave, result);
			size += RVGenerateHelper.argStackSize(args);
			size = MathUtil.roundTo(size, RVRegUtil.DEFAULT_STACK_ALIGNMENT);
			RVGenerateHelper.subtractSp(size, result);
			RVGenerateHelper.storeArgs(rvModule, args, cacheManager, result);
			cacheManager.invalidateAll(result);

			result.addLine(String.format("call %s", ((IFunction) inst.getFunction()).getName()));

			filterAliveAfter(inst);
			if (ret != null) RVGenerateHelper.collectReturn(ret, cacheManager, result);

			RVGenerateHelper.addSp(size, result);
			RVGenerateHelper.restoreRegisters(toSave, result);
			return null;
		}

		@Override
		public Void visit(Call inst) {
			return handleCall(inst, RVGenerateHelper.typedPositionOf(rvModule, inst));
		}

		@Override
		public Void visit(CallVoid inst) {
			return handleCall(inst, null);
		}

		private RVRegister loadRegister(Value value) {
			var pos = Objects.requireNonNull(RVGenerateHelper.typedPositionOf(rvModule, value));
			return cacheManager.load(pos, result);
		}

		private RVRegister currentRegister(Instruction inst) {
			filterAliveAfter(inst);
			var posCur = Objects.requireNonNull(RVGenerateHelper.typedPositionOf(rvModule, (Value) inst));
			return cacheManager.allocForStore(posCur, result);
		}

		private Void handleBinary(DummyBinary inst, String operator) {
			var regX = loadRegister(inst.getX());
			var regY = loadRegister(inst.getY());
			cacheManager.unlock(regX);
			cacheManager.unlock(regY);
			var regCur = currentRegister(inst);
			result.addLine(String.format("%s %s, %s, %s", operator, regCur, regX, regY));
			cacheManager.unlock(regCur);
			return null;
		}

		private Void handleImmBinary(RVImmBinary inst, String operator) {
			var regX = loadRegister(inst.getX());
			cacheManager.unlock(regX);
			var regCur = currentRegister(inst);
			result.addLine(String.format("%s %s, %s, %d", operator, regCur, regX, inst.y));
			cacheManager.unlock(regCur);
			return null;
		}

		private Void handleUnary(DummyUnary inst, String operator, String suffix) {
			var regX = loadRegister(inst.getX());
			cacheManager.unlock(regX);
			var regCur = currentRegister(inst);
			result.addLine(String.format("%s %s, %s%s", operator, regCur, regX, suffix));
			cacheManager.unlock(regCur);
			return null;
		}

		private Void handleUnary(DummyUnary inst, String operator) {
			return handleUnary(inst, operator, "");
		}

		private Void handleTriple(DummyTriple inst, String operator) {
			var regX = loadRegister(inst.getX());
			var regY = loadRegister(inst.getY());
			var regZ = loadRegister(inst.getZ());
			cacheManager.unlock(regX);
			cacheManager.unlock(regY);
			cacheManager.unlock(regZ);
			var regCur = currentRegister(inst);
			result.addLine(String.format("%s %s, %s, %s, %s", operator, regCur, regX, regY, regZ));
			cacheManager.unlock(regCur);
			return null;
		}

		@Override
		public Void visit(RVAdd inst) {
			return handleBinary(inst, "add");
		}

		@Override
		public Void visit(RVAddImm inst) {
			return handleImmBinary(inst, "addi");
		}

		@Override
		public Void visit(RVAddWord inst) {
			return handleBinary(inst, "addw");
		}

		@Override
		public Void visit(RVAddWordImm inst) {
			return handleImmBinary(inst, "addiw");
		}

		@Override
		public Void visit(RVAnd inst) {
			return handleBinary(inst, "and");
		}

		@Override
		public Void visit(RVAndImm inst) {
			return handleImmBinary(inst, "andi");
		}

		@Override
		public Void visit(RVCvtWord2Float inst) {
			return handleUnary(inst, "fcvt.s.w");
		}

		@Override
		public Void visit(RVCvtDWord2Float inst) {
			return handleUnary(inst, "fcvt.s.l");
		}

		@Override
		public Void visit(RVCvtFloat2Word inst) {
			return handleUnary(inst, "fcvt.w.s", ", rtz");
		}

		@Override
		public Void visit(RVCvtFloat2DWord inst) {
			return handleUnary(inst, "fcvt.l.s", ", rtz");
		}

		@Override
		public Void visit(RVDiv inst) {
			return handleBinary(inst, "div");
		}

		@Override
		public Void visit(RVDivWord inst) {
			return handleBinary(inst, "divw");
		}

		@Override
		public Void visit(RVFloatAdd inst) {
			return handleBinary(inst, "fadd.s");
		}

		@Override
		public Void visit(RVFloatDiv inst) {
			return handleBinary(inst, "fdiv.s");
		}

		@Override
		public Void visit(RVFloatEq inst) {
			return handleBinary(inst, "feq.s");
		}

		@Override
		public Void visit(RVFloatLe inst) {
			return handleBinary(inst, "fle.s");
		}

		@Override
		public Void visit(RVFloatLt inst) {
			return handleBinary(inst, "flt.s");
		}

		@Override
		public Void visit(RVFloatMul inst) {
			return handleBinary(inst, "fmul.s");
		}

		@Override
		public Void visit(RVFloatMulAdd inst) {
			return handleTriple(inst, "fmadd.s");
		}

		@Override
		public Void visit(RVFloatMulSub inst) {
			return handleTriple(inst, "fmsub.s");
		}

		@Override
		public Void visit(RVFloatNeg inst) {
			var regX = loadRegister(inst.getX());
			cacheManager.unlock(regX);
			var regCur = currentRegister(inst);
			result.addLine(String.format("fsgnjn.s %s, %s, %s", regCur, regX, regX));
			cacheManager.unlock(regCur);
			return null;
		}

		@Override
		public Void visit(RVFloatNegMulAdd inst) {
			return handleTriple(inst, "fnmadd.s");
		}

		@Override
		public Void visit(RVFloatNegMulSub inst) {
			return handleTriple(inst, "fnmsub.s");
		}

		@Override
		public Void visit(RVFloatSub inst) {
			return handleBinary(inst, "fsub.s");
		}

		@Override
		public Void visit(RVLoadAddress inst) {
			var regCur = currentRegister(inst);
			result.addLine(String.format("la %s, %s", regCur, inst.getGlobal().getName()));
			cacheManager.unlock(regCur);
			return null;
		}

		private Void handleLoad(RVLoad inst, String operator) {
			var regPtr = loadRegister(inst.getPointer());
			cacheManager.unlock(regPtr);
			var regCur = currentRegister(inst);
			result.addLine(String.format("%s %s, %d(%s)", operator, regCur, inst.offset, regPtr));
			cacheManager.unlock(regCur);
			return null;
		}

		@Override
		public Void visit(RVLoadDWord inst) {
			return handleLoad(inst, "ld");
		}

		@Override
		public Void visit(RVLoadImm inst) {
			var regCur = currentRegister(inst);
			result.addLine(String.format("li %s, %s", regCur, inst.val));
			cacheManager.unlock(regCur);
			return null;
		}

		@Override
		public Void visit(RVLoadFloat inst) {
			return handleLoad(inst, "flw");
		}

		@Override
		public Void visit(RVLoadWord inst) {
			return handleLoad(inst, "lw");
		}

		@Override
		public Void visit(RVMul inst) {
			return handleBinary(inst, "mul");
		}

		@Override
		public Void visit(RVMulWord inst) {
			return handleBinary(inst, "mulw");
		}

		@Override
		public Void visit(RVOr inst) {
			return handleBinary(inst, "or");
		}

		@Override
		public Void visit(RVOrImm inst) {
			return handleImmBinary(inst, "ori");
		}

		@Override
		public Void visit(RVRem inst) {
			return handleBinary(inst, "rem");
		}

		@Override
		public Void visit(RVRemWord inst) {
			return handleBinary(inst, "remw");
		}

		@Override
		public Void visit(RVSetLessThan inst) {
			return handleBinary(inst, "slt");
		}

		@Override
		public Void visit(RVShiftLeft inst) {
			return handleBinary(inst, "sll");
		}

		@Override
		public Void visit(RVShiftLeftImm inst) {
			return handleImmBinary(inst, "slli");
		}

		@Override
		public Void visit(RVShiftLeftWord inst) {
			return handleBinary(inst, "sllw");
		}

		@Override
		public Void visit(RVShiftLeftWordImm inst) {
			return handleImmBinary(inst, "slliw");
		}

		@Override
		public Void visit(RVShiftRightArith inst) {
			return handleBinary(inst, "sra");
		}

		@Override
		public Void visit(RVShiftRightArithImm inst) {
			return handleImmBinary(inst, "srai");
		}

		@Override
		public Void visit(RVShiftRightArithWord inst) {
			return handleBinary(inst, "sraw");
		}

		@Override
		public Void visit(RVShiftRightArithWordImm inst) {
			return handleImmBinary(inst, "sraiw");
		}

		@Override
		public Void visit(RVShiftRightLogical inst) {
			return handleBinary(inst, "srl");
		}

		@Override
		public Void visit(RVShiftRightLogicalImm inst) {
			return handleImmBinary(inst, "srli");
		}

		@Override
		public Void visit(RVShiftRightLogicalWord inst) {
			return handleBinary(inst, "srlw");
		}

		@Override
		public Void visit(RVShiftRightLogicalWordImm inst) {
			return handleImmBinary(inst, "srliw");
		}

		private Void handleStore(RVStore inst, String operator) {
			var regSrc = loadRegister(inst.getSrc());
			var regDest = loadRegister(inst.getDest());
			cacheManager.unlock(regSrc);
			cacheManager.unlock(regDest);
			result.addLine(String.format("%s %s, %d(%s)", operator, regSrc, inst.offset, regDest));
			return null;
		}

		@Override
		public Void visit(RVStoreDWord inst) {
			return handleStore(inst, "sd");
		}

		@Override
		public Void visit(RVStoreFloat inst) {
			return handleStore(inst, "fsw");
		}

		@Override
		public Void visit(RVStoreWord inst) {
			return handleStore(inst, "sw");
		}

		@Override
		public Void visit(RVSub inst) {
			return handleBinary(inst, "sub");
		}

		@Override
		public Void visit(RVSubWord inst) {
			return handleBinary(inst, "subw");
		}

		@Override
		public Void visit(RVXor inst) {
			return handleBinary(inst, "xor");
		}

		@Override
		public Void visit(RVParallelMove inst) {
			var move = new HashMap<RVTypedPosition, RVTypedPosition>();
			inst.getMoves().forEach((to, from) -> {
				var posTo = Objects.requireNonNull(RVGenerateHelper.typedPositionOf(rvModule, to));
				var posFrom = Objects.requireNonNull(RVGenerateHelper.typedPositionOf(rvModule, from));
				move.put(posTo, posFrom);
			});
			RVGenerateHelper.parallelMove(move, cacheManager, result);
			return null;
		}

		@Override
		public Void visit(RVRegPlaceholder inst) {
			return null; // Do nothing
		}

		@Override
		public Void visit(RVXorImm inst) {
			return handleImmBinary(inst, "xori");
		}

		@Override
		public Void visit(RVMoveWord2Float inst) {
			return handleUnary(inst, "fmv.w.x");
		}
	}
}