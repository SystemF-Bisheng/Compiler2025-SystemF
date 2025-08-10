package org.systemf.compiler.ir;

import java.util.ListIterator;

import org.systemf.compiler.hir.value.instruction.nonterminal.scf.Break;
import org.systemf.compiler.hir.value.instruction.nonterminal.scf.For;
import org.systemf.compiler.hir.value.instruction.nonterminal.scf.If;
import org.systemf.compiler.hir.value.instruction.nonterminal.scf.While;
import org.systemf.compiler.hir.value.instruction.nonterminal.scf.Yield;
import org.systemf.compiler.hir.value.loop.IndexValue;
import org.systemf.compiler.hir.value.loop.LoopCarrier;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.ExternalFunction;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.global.IFunction;
import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.FunctionType;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.type.UnsizedArray;
import org.systemf.compiler.ir.type.Void;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Parameter;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.ArrayZeroInitializer;
import org.systemf.compiler.ir.value.constant.ConcreteArray;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.constant.ConstantArray;
import org.systemf.compiler.ir.value.constant.ConstantFloat;
import org.systemf.compiler.ir.value.constant.ConstantInt32;
import org.systemf.compiler.ir.value.constant.ConstantInt64;
import org.systemf.compiler.ir.value.constant.Undefined;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.AShr;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.And;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.LShr;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.Or;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.Shl;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.Xor;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.FpToSi32;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.PtrCast;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.Si32ToFp;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.Si32ToSi64;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.Si64ToSi32;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FAdd;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FCmp;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FDiv;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FMul;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FNeg;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FSub;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Add;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.ICmp;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Mul;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.SDiv;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.SRem;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Sub;
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
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;
import org.systemf.compiler.ir.value.instruction.terminal.Unreachable;

/**
 * external interface. all write operations are available with only Module and IRBuilder
 */
public class IRBuilder implements AutoCloseable {
	public final IRFolder folder;
	private final Module module;

	private ListIterator<Instruction> position;

	public IRBuilder(Module module) {
		if (module.isIRBuilderAttached()) throw new IllegalStateException("Module has already been attached");
		module.attachIRBuilder();

		this.module = module;
		this.folder = new IRFolder(this);
	}

	public Void buildVoidType() {
		return Void.INSTANCE;
	}

	public Parameter buildParameter(Type type, String name) {
		return new Parameter(type, module.getNonConflictName(name));
	}

	public I32 buildI32Type() {
		return I32.INSTANCE;
	}

	public Float buildFloatType() {
		return Float.INSTANCE;
	}

	public Pointer buildPointerType(Type elementType) {
		return new Pointer(elementType);
	}

	public Array buildArrayType(Sized elementType, int length) {
		return new Array(length, elementType);
	}

	public UnsizedArray buildUnsizedArrayType(Sized elementType) {
		return new UnsizedArray(elementType);
	}

	public Undefined buildUndefined(Sized type) {
		return Undefined.of(type);
	}

	public ConstantInt32 buildConstantInt32(long value) {
		return ConstantInt32.valueOf(value);
	}

	public ConstantInt64 buildConstantInt64(long value) {
		return ConstantInt64.valueOf(value);
	}

	public Constant buildConstantZero(int width) {
		return buildConstantInt(0, width);
	}

	public Constant buildConstantInt(long value, int width) {
		if (width == 32) return buildConstantInt32(value);
		else if (width == 64) return buildConstantInt64(value);
		else throw new IllegalArgumentException("Unsupported width: " + width);
	}

	public ConstantFloat buildConstantFloat(double value) {
		return ConstantFloat.valueOf(value);
	}

	public ConstantArray buildConstantArray(Sized elementType, Constant... content) {
		return new ConcreteArray(elementType, content);
	}

	public ArrayZeroInitializer buildConstantArray(Sized elementType, int size) {
		return new ArrayZeroInitializer(elementType, size);
	}

	public GlobalVariable buildGlobalVariable(String name, Sized type, Constant initializer) {
		GlobalVariable declaration = new GlobalVariable(name, type, initializer);
		module.addGlobalVariable(declaration);
		return declaration;
	}

	public Function buildFunction(String name, Type returnType, Parameter... formalArgs) {
		Function function = new Function(name, returnType, formalArgs);
		var entry = buildBasicBlock(function, name + "Entry");
		function.setEntryBlock(entry);
		module.addFunction(function);
		return function;
	}

	public FunctionType buildFunctionType(Type returnType, Type... params) {
		return new FunctionType(returnType, params);
	}

	public ExternalFunction buildExternalFunction(String name, Type returnType, Type... params) {
		ExternalFunction function = new ExternalFunction(name, buildFunctionType(returnType, params));
		module.addExternalFunction(function);
		return function;
	}

	public BasicBlock buildBasicBlock(Function func, String name) {
		BasicBlock block = new BasicBlock(module.getNonConflictName(name));
		func.insertBlock(block);
		return block;
	}

	public Ret buildRet(Value value) {
		Ret ret = new Ret(value);
		insertInstruction(ret);
		return ret;
	}

	public RetVoid buildRetVoid() {
		RetVoid retVoid = RetVoid.INSTANCE;
		insertInstruction(retVoid);
		return retVoid;
	}

	public And buildAnd(Value lhs, Value rhs, String name) {
		And andInst = new And(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(andInst);
		return andInst;
	}

	public Value buildOrFoldAnd(Value lhs, Value rhs, String name) {
		return folder.tryFoldAnd(lhs, rhs).map(c -> (Value) c).orElseGet(() -> buildAnd(lhs, rhs, name));
	}

	public Or buildOr(Value lhs, Value rhs, String name) {
		Or orInst = new Or(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(orInst);
		return orInst;
	}

	public Value buildOrFoldOr(Value lhs, Value rhs, String name) {
		return folder.tryFoldOr(lhs, rhs).map(c -> (Value) c).orElseGet(() -> buildOr(lhs, rhs, name));
	}

	public AShr buildAShr(Value lhs, Value rhs, String name) {
		AShr AShrInst = new AShr(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(AShrInst);
		return AShrInst;
	}

	public Value buildOrFoldAShr(Value lhs, Value rhs, String name) {
		return folder.tryFoldAShr(lhs, rhs).map(c -> (Value) c).orElseGet(() -> buildAShr(lhs, rhs, name));
	}

	public Shl buildShl(Value lhs, Value rhs, String name) {
		Shl shlInst = new Shl(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(shlInst);
		return shlInst;
	}

	public Value buildOrFoldShl(Value lhs, Value rhs, String name) {
		return folder.tryFoldShl(lhs, rhs).map(c -> (Value) c).orElseGet(() -> buildShl(lhs, rhs, name));
	}

	public Xor buildXor(Value lhs, Value rhs, String name) {
		Xor xorInst = new Xor(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(xorInst);
		return xorInst;
	}

	public Value buildOrFoldXor(Value lhs, Value rhs, String name) {
		return folder.tryFoldXor(lhs, rhs).map(c -> (Value) c).orElseGet(() -> buildXor(lhs, rhs, name));
	}

	public LShr buildLShr(Value lhs, Value rhs, String name) {
		LShr LShrInstruction = new LShr(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(LShrInstruction);
		return LShrInstruction;
	}

	public Value buildOrFoldLShr(Value lhs, Value rhs, String name) {
		return folder.tryFoldLShr(lhs, rhs).map(c -> (Value) c).orElseGet(() -> buildLShr(lhs, rhs, name));
	}

	public Add buildAdd(Value lhs, Value rhs, String name) {
		Add addInst = new Add(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(addInst);
		return addInst;
	}

	public Value buildOrFoldAdd(Value lhs, Value rhs, String name) {
		return folder.tryFoldAdd(lhs, rhs).map(c -> (Value) c).orElseGet(() -> buildAdd(lhs, rhs, name));
	}

	public Sub buildSub(Value lhs, Value rhs, String name) {
		Sub subInst = new Sub(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(subInst);
		return subInst;
	}

	public Value buildOrFoldSub(Value lhs, Value rhs, String name) {
		return folder.tryFoldSub(lhs, rhs).map(c -> (Value) c).orElseGet(() -> buildSub(lhs, rhs, name));
	}

	public Mul buildMul(Value lhs, Value rhs, String name) {
		Mul mulInst = new Mul(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(mulInst);
		return mulInst;
	}

	public Value buildOrFoldMul(Value lhs, Value rhs, String name) {
		return folder.tryFoldMul(lhs, rhs).map(c -> (Value) c).orElseGet(() -> buildMul(lhs, rhs, name));
	}

	public SDiv buildSDiv(Value lhs, Value rhs, String name) {
		SDiv sDivInst = new SDiv(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(sDivInst);
		return sDivInst;
	}

	public Value buildOrFoldSDiv(Value lhs, Value rhs, String name) {
		return folder.tryFoldSDiv(lhs, rhs).map(c -> (Value) c).orElseGet(() -> buildSDiv(lhs, rhs, name));
	}

	public SRem buildSRem(Value lhs, Value rhs, String name) {
		SRem sRemInst = new SRem(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(sRemInst);
		return sRemInst;
	}

	public Value buildOrFoldSRem(Value lhs, Value rhs, String name) {
		return folder.tryFoldSRem(lhs, rhs).map(c -> (Value) c).orElseGet(() -> buildSRem(lhs, rhs, name));
	}

	public ICmp buildICmp(Value op1, Value op2, String name, CompareOp code) {
		ICmp iCmpInst = new ICmp(module.getNonConflictName(name), code, op1, op2);
		insertInstruction(iCmpInst);
		return iCmpInst;
	}

	public Value buildOrFoldICmp(Value op1, Value op2, String name, CompareOp code) {
		return folder.tryFoldICmp(op1, op2, code).map(c -> (Value) c).orElseGet(() -> buildICmp(op1, op2, name, code));
	}

	public FAdd buildFAdd(Value lhs, Value rhs, String name) {
		FAdd fAddInst = new FAdd(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(fAddInst);
		return fAddInst;
	}

	public Value buildOrFoldFAdd(Value lhs, Value rhs, String name) {
		return folder.tryFoldFAdd(lhs, rhs).map(c -> (Value) c).orElseGet(() -> buildFAdd(lhs, rhs, name));
	}

	public FMul buildFMul(Value lhs, Value rhs, String name) {
		FMul fMulInst = new FMul(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(fMulInst);
		return fMulInst;
	}

	public Value buildOrFoldFMul(Value lhs, Value rhs, String name) {
		return folder.tryFoldFMul(lhs, rhs).map(c -> (Value) c).orElseGet(() -> buildFMul(lhs, rhs, name));
	}

	public FSub buildFSub(Value lhs, Value rhs, String name) {
		FSub fSubInst = new FSub(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(fSubInst);
		return fSubInst;
	}

	public Value buildOrFoldFSub(Value lhs, Value rhs, String name) {
		return folder.tryFoldFSub(lhs, rhs).map(c -> (Value) c).orElseGet(() -> buildFSub(lhs, rhs, name));
	}

	public FDiv buildFDiv(Value lhs, Value rhs, String name) {
		FDiv fDivInst = new FDiv(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(fDivInst);
		return fDivInst;
	}

	public Value buildOrFoldFDiv(Value lhs, Value rhs, String name) {
		return folder.tryFoldFDiv(lhs, rhs).map(c -> (Value) c).orElseGet(() -> buildFDiv(lhs, rhs, name));
	}

	public FNeg buildFNeg(Value op, String name) {
		FNeg fNegInst = new FNeg(module.getNonConflictName(name), op);
		insertInstruction(fNegInst);
		return fNegInst;
	}

	public Value buildOrFoldFNeg(Value op, String name) {
		return folder.tryFoldFNeg(op).map(c -> (Value) c).orElseGet(() -> buildFNeg(op, name));
	}

	public FCmp buildFCmp(Value lhs, Value rhs, String name, CompareOp code) {
		FCmp fCmpInst = new FCmp(module.getNonConflictName(name), code, lhs, rhs);
		insertInstruction(fCmpInst);
		return fCmpInst;
	}

	public Value buildOrFoldFCmp(Value lhs, Value rhs, String name, CompareOp code) {
		return folder.tryFoldFCmp(lhs, rhs, code).map(c -> (Value) c).orElseGet(() -> buildFCmp(lhs, rhs, name, code));
	}

	public PtrCast buildPtrCast(Value x, Type type, String name) {
		var inst = new PtrCast(module.getNonConflictName(name), x, type);
		insertInstruction(inst);
		return inst;
	}

	public FpToSi32 buildFpToSi32(Value op, String name) {
		FpToSi32 fpToSiInst = new FpToSi32(module.getNonConflictName(name), op);
		insertInstruction(fpToSiInst);
		return fpToSiInst;
	}

	public Value buildOrFoldFpToSi32(Value op, String name) {
		return folder.tryFoldFpToSi32(op).map(c -> (Value) c).orElseGet(() -> buildFpToSi32(op, name));
	}

	public Si32ToFp buildSi32ToFp(Value op, String name) {
		Si32ToFp siToFpInst = new Si32ToFp(module.getNonConflictName(name), op);
		insertInstruction(siToFpInst);
		return siToFpInst;
	}

	public Value buildOrFoldSi32ToFp(Value op, String name) {
		return folder.tryFoldSi32ToFp(op).map(c -> (Value) c).orElseGet(() -> buildSi32ToFp(op, name));
	}

	public Si32ToSi64 buildSi32ToSi64(Value op, String name) {
		Si32ToSi64 si32ToSi64Inst = new Si32ToSi64(module.getNonConflictName(name), op);
		insertInstruction(si32ToSi64Inst);
		return si32ToSi64Inst;
	}

	public Value buildOrFoldSi32ToSi64(Value op, String name) {
		return folder.tryFoldSi32ToSi64(op).map(c -> (Value) c).orElseGet(() -> buildSi32ToSi64(op, name));
	}

	public Si64ToSi32 buildSi64ToSi32(Value op, String name) {
		Si64ToSi32 si64ToSi32Inst = new Si64ToSi32(module.getNonConflictName(name), op);
		insertInstruction(si64ToSi32Inst);
		return si64ToSi32Inst;
	}

	public Value buildOrFoldSi64ToSi32(Value op, String name) {
		return folder.tryFoldSi64ToSi32(op).map(c -> (Value) c).orElseGet(() -> buildSi64ToSi32(op, name));
	}

	public Call buildCall(IFunction function, String name, Value... args) {
		Call callInst = new Call(module.getNonConflictName(name), function, args);
		insertInstruction(callInst);
		return callInst;
	}

	public CallVoid buildCallVoid(IFunction function, Value... args) {
		CallVoid callInst = new CallVoid(function, args);
		insertInstruction(callInst);
		return callInst;
	}

	public Alloca buildAlloca(Sized type, String name) {
		Alloca allocaInst = new Alloca(module.getNonConflictName(name), type);
		insertInstruction(allocaInst);
		return allocaInst;
	}

	public GetPtr buildGetPtr(Value array, Value index, String name) {
		GetPtr getPtrInst = new GetPtr(module.getNonConflictName(name), array, index);
		insertInstruction(getPtrInst);
		return getPtrInst;
	}

	public Load buildLoad(Value pointer, String name) {
		Load loadInst = new Load(module.getNonConflictName(name), pointer);
		insertInstruction(loadInst);
		return loadInst;
	}

	public Store buildStore(Value src, Value dest) {
		Store storeInst = new Store(src, dest);
		insertInstruction(storeInst);
		return storeInst;
	}

	public Phi buildPhi(Type type, String name) {
		var phiInst = new Phi(type, module.getNonConflictName(name));
		insertInstruction(phiInst);
		return phiInst;
	}

	public Unreachable buildUnreachable() {
		Unreachable unreachableInst = Unreachable.INSTANCE;
		insertInstruction(unreachableInst);
		return unreachableInst;
	}

	public Br buildBr(BasicBlock target) {
		Br brInst = new Br(target);
		insertInstruction(brInst);
		return brInst;
	}

	public CondBr buildCondBr(Value condition, BasicBlock trueTarget, BasicBlock falseTarget) {
		CondBr condBrInst = new CondBr(condition, trueTarget, falseTarget);
		insertInstruction(condBrInst);
		return condBrInst;
	}

	public Break buildBreak() {
		Break _break = new Break();
		insertInstruction(_break);
		return _break;
	}

	public For buildFor(IndexValue index, LoopCarrier... loopCarriers) {
		For _for = new For(index, loopCarriers);
		insertInstruction(_for);
		return _for;
	}

	public If buildIf(boolean hasElse) {
		If _if = new If(hasElse);
		insertInstruction(_if);
		return _if;
	}

	public While buildWhile(LoopCarrier... loopCarriers) {
		While _while = new While(loopCarriers);
		insertInstruction(_while);
		return _while;
	}

	public Yield buildYield(Value... yieldValues) {
		Yield yield = new Yield(yieldValues);
		insertInstruction(yield);
		return yield;
	}

	public Terminal buildOrFoldCondBr(Value condition, BasicBlock trueTarget, BasicBlock falseTarget) {
		return folder.tryFoldCondBr(condition, trueTarget, falseTarget).map(block -> (Terminal) this.buildBr(block))
				.orElseGet(() -> buildCondBr(condition, trueTarget, falseTarget));
	}

	public void attachToBlockTail(BasicBlock block) {
		position = block.instructions.listIterator(block.instructions.size());
	}

	public void setPosition(ListIterator<Instruction> position) {
		this.position = position;
	}

	protected void insertInstruction(Instruction inst) {
		position.add(inst);
	}

	@Override
	public void close() {
		module.detachIRBuilder();
	}
}