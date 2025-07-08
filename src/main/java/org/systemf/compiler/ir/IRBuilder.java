package org.systemf.compiler.ir;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalDeclaration;
import org.systemf.compiler.ir.global.initializer.ArrayInitializer;
import org.systemf.compiler.ir.global.initializer.AtomicInitializer;
import org.systemf.compiler.ir.global.initializer.IGlobalInitializer;
import org.systemf.compiler.ir.type.*;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.Void;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Parameter;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.constant.ConstantFloat;
import org.systemf.compiler.ir.value.constant.ConstantInt;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.AShr;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.And;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.LShr;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.Shl;
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
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Unreachable;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.ir.value.instruction.terminal.Ret;
import org.systemf.compiler.ir.value.instruction.terminal.RetVoid;

/**
 * external interface. all write operations are available with only Module and IRBuilder
 */
public class IRBuilder implements AutoCloseable {
	private final Module module;
	private BasicBlock currentBlock;

	public IRBuilder(Module module) {
		if (module.isIRBuilderAttached()) throw new IllegalStateException("Module has already been attached");
		module.attachIRBuilder();

		this.module = module;
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

	public ConstantInt buildConstantInt(int value) {
		return new ConstantInt(value);
	}

	public ConstantFloat buildConstantFloat(float value) {
		return new ConstantFloat(value);
	}

	public GlobalDeclaration buildGlobalDeclaration(String name, Type type, IGlobalInitializer initializer) {
		GlobalDeclaration declaration = new GlobalDeclaration(module.getNonConflictName(name), type, initializer);
		module.addGlobalDeclaration(declaration);
		return declaration;
	}

	public IGlobalInitializer buildGlobalInitializer(Constant value) {
		return new AtomicInitializer(value);
	}

	public IGlobalInitializer buildGlobalInitializer(int length, IGlobalInitializer... elements) {
		return new ArrayInitializer(length, elements);
	}

	public Function buildFunction(String name, Type returnType, Parameter... formalArgs) {
		Function function = new Function(module.getNonConflictName(name), returnType, formalArgs);
		module.addFunction(function);
		return function;
	}

	public BasicBlock buildBasicBlock(Function func, String name) {
		BasicBlock block = new BasicBlock(module.getNonConflictName(name));
		func.insertBlock(block);
		return block;
	}

	public void buildRet(Value value) {
		insertInstruction(new Ret(value));
	}

	public void buildRetVoid() {
		insertInstruction(new RetVoid());
	}

	public And buildAnd(Value lhs, Value rhs, String name) {
		And andInst = new And(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(andInst);
		return andInst;
	}

	public AShr buildAShr(Value lhs, Value rhs, String name) {
		AShr AShrInst = new AShr(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(AShrInst);
		return AShrInst;
	}

	public Shl buildShl(Value lhs, Value rhs, String name) {
		Shl shlInst = new Shl(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(shlInst);
		return shlInst;
	}

	public And buildXor(Value lhs, Value rhs, String name) {
		And xorInst = new And(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(xorInst);
		return xorInst;
	}

	public LShr buildLShr(Value lhs, Value rhs, String name) {
		LShr LShrInstruction = new LShr(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(LShrInstruction);
		return LShrInstruction;
	}

	public Add buildAdd(Value lhs, Value rhs, String name) {
		Add addInst = new Add(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(addInst);
		return addInst;
	}

	public Sub buildSub(Value lhs, Value rhs, String name) {
		Sub subInst = new Sub(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(subInst);
		return subInst;
	}

	public Mul buildMul(Value lhs, Value rhs, String name) {
		Mul mulInst = new Mul(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(mulInst);
		return mulInst;
	}

	public SDiv buildSDiv(Value lhs, Value rhs, String name) {
		SDiv sDivInst = new SDiv(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(sDivInst);
		return sDivInst;
	}

	public SRem buildSRem(Value lhs, Value rhs, String name) {
		SRem sRemInst = new SRem(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(sRemInst);
		return sRemInst;
	}

	public ICmp buildICmp(Value op1, Value op2, String name, CompareOp code) {
		ICmp iCmpInst = new ICmp(module.getNonConflictName(name), code, op1, op2);
		insertInstruction(iCmpInst);
		return iCmpInst;

	}

	public FAdd buildFAdd(Value lhs, Value rhs, String name) {
		FAdd fAddInst = new FAdd(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(fAddInst);
		return fAddInst;
	}

	public FMul buildFMul(Value lhs, Value rhs, String name) {
		FMul fMulInst = new FMul(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(fMulInst);
		return fMulInst;
	}

	public FSub buildFSub(Value lhs, Value rhs, String name) {
		FSub fSubInst = new FSub(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(fSubInst);
		return fSubInst;
	}

	public FDiv buildFDiv(Value lhs, Value rhs, String name) {
		FDiv fDivInst = new FDiv(module.getNonConflictName(name), lhs, rhs);
		insertInstruction(fDivInst);
		return fDivInst;
	}

	public FNeg buildFNeg(Value op, String name) {
		FNeg fNegInst = new FNeg(module.getNonConflictName(name), op);
		insertInstruction(fNegInst);
		return fNegInst;
	}

	public FCmp buildFCmp(Value lhs, Value rhs, String name, CompareOp code) {
		FCmp fCmpInst = new FCmp(module.getNonConflictName(name), code, lhs, rhs);
		insertInstruction(fCmpInst);
		return fCmpInst;
	}

	public FpToSi buildFpToSi(Value op, String name) {
		FpToSi fpToSiInst = new FpToSi(module.getNonConflictName(name), op);
		insertInstruction(fpToSiInst);
		return fpToSiInst;
	}

	public SiToFp buildSiToFp(Value op, String name) {
		SiToFp siToFpInst = new SiToFp(module.getNonConflictName(name), op);
		insertInstruction(siToFpInst);
		return siToFpInst;
	}

	public Call buildCall(Function function, String name, Value... args) {
		Call callInst = new Call(module.getNonConflictName(name), function, args);
		insertInstruction(callInst);
		return callInst;
	}

	public CallVoid buildCallVoid(Function function, Value... args) {
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

	public Unreachable buildUnreachable() {
		Unreachable unreachableInst = new Unreachable();
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

	public void attachToBlockTail(BasicBlock block) {
		currentBlock = block;
	}

	private void insertInstruction(Instruction inst) {
		if (currentBlock == null)
			throw new IllegalArgumentException("Attempt to insert an instruction without an attached block");
		currentBlock.insertInstruction(inst);
	}

	@Override
	public void close() {
		module.detachIRBuilder();
	}
}