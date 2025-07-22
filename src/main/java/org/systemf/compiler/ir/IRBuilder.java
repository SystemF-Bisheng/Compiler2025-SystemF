package org.systemf.compiler.ir;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.ExternalFunction;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.global.IFunction;
import org.systemf.compiler.ir.type.*;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.Void;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Parameter;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.*;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.FpToSi;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.SiToFp;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.Call;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.CallVoid;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Alloca;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.GetPtr;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.ir.value.instruction.terminal.*;

import java.util.ListIterator;

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

	public Undefined buildUndefined(Type type) {
		return Undefined.of(type);
	}

	public ConstantInt buildConstantInt(long value) {
		return ConstantInt.valueOf(value);
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

	public GlobalVariable buildGlobalVariable(String name, Type type, Constant initializer) {
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

	public FpToSi buildFpToSi(Value op, String name) {
		FpToSi fpToSiInst = new FpToSi(module.getNonConflictName(name), op);
		insertInstruction(fpToSiInst);
		return fpToSiInst;
	}

	public Value buildOrFoldFpToSi(Value op, String name) {
		return folder.tryFoldFpToSi(op).map(c -> (Value) c).orElseGet(() -> buildFpToSi(op, name));
	}

	public SiToFp buildSiToFp(Value op, String name) {
		SiToFp siToFpInst = new SiToFp(module.getNonConflictName(name), op);
		insertInstruction(siToFpInst);
		return siToFpInst;
	}

	public Value buildOrFoldSiToFp(Value op, String name) {
		return folder.tryFoldSiToFp(op).map(c -> (Value) c).orElseGet(() -> buildSiToFp(op, name));
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

	public Br constructBr(BasicBlock target) {
		return new Br(target);
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

	public Terminal buildOrFoldCondBr(Value condition, BasicBlock trueTarget, BasicBlock falseTarget) {
		return folder.tryFoldCondBr(condition, trueTarget, falseTarget)
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