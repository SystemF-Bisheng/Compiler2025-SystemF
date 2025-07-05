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
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.ConstantFloat;
import org.systemf.compiler.ir.value.constant.ConstantInt;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.AShr;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.And;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.LShr;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.Shl;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.FptoSi;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.SitoFp;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.Call;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Alloca;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Getptr;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Unreachable;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.ir.value.instruction.terminal.Ret;
import org.systemf.compiler.ir.value.instruction.terminal.RetVoid;

import java.util.HashSet;
import java.util.Set;

/**
 * external interface. all write operations are available with only Module and IRBuilder
 */
public class IRBuilder {
	private final Module module;
	private final Set<String> occupiedNames;
	private BasicBlock currentBlock;

	public IRBuilder(Module module) {
		if (module.isIRBuilderAttached()) {
			System.err.println("warning: multiple IRBuilder attaching to one Module may cause unexpected behavior");
		}
		module.attachIRBuilder();

		this.module = module;
		occupiedNames = new HashSet<>();
	}

		public Void buildVoidType() {
		return Void.INSTANCE;
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

	public Array buildArrayType(int length, Type elementType) {
		return new Array(length, elementType);
	}

	public ConstantInt buildConstantInt(int value) {
		return new ConstantInt(value);
	}

	public ConstantFloat buildConstantFloat(float value) {
		return new ConstantFloat(value);
	}


	public GlobalDeclaration buildGlobalDeclaration(String name, I32 type, IGlobalInitializer initializer) {
		GlobalDeclaration declaration = new GlobalDeclaration(getNonConflictName(name), type, initializer);
		module.addGlobalDeclaration(declaration);
		return declaration;
	}

	public GlobalDeclaration buildGlobalDeclaration(String name, Array type, IGlobalInitializer initializer) {
		GlobalDeclaration declaration = new GlobalDeclaration(getNonConflictName(name), type, initializer);
		module.addGlobalDeclaration(declaration);
		return declaration;
	}

	public IGlobalInitializer buildGlobalInitializer(ConstantInt value) {
		return new AtomicInitializer(value);
	}

	public IGlobalInitializer buildGlobalInitializer(int length, IGlobalInitializer... elements) {
		return new ArrayInitializer(length, elements);
	}

	public Function buildFunction(String name, FunctionType type) {
		Function function = new Function(getNonConflictName(name), type);
		module.addFunction(new Function(getNonConflictName(name), type));
		return function;
	}

	public BasicBlock buildBasicBlock(Function func ,String name) {
		BasicBlock block = new BasicBlock(getNonConflictName(name));
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
		And andInst = new And(getNonConflictName(name),lhs, rhs);
		insertInstruction(andInst);
		return andInst;
	}

	public AShr buildAShr(Value lhs, Value rhs, String name) {
		AShr AShrInst = new AShr(getNonConflictName(name), lhs, rhs);
		insertInstruction(AShrInst);
		return AShrInst;
	}

	public Shl buildShl(Value lhs, Value rhs, String name) {
		Shl shlInst = new Shl(getNonConflictName(name), lhs, rhs);
		insertInstruction(shlInst);
		return shlInst;
	}

	public And buildXor(Value lhs, Value rhs, String name) {
		And xorInst = new And(getNonConflictName(name), lhs, rhs);
		insertInstruction(xorInst);
		return xorInst;
	}

	public LShr buildLShr(Value lhs, Value rhs, String name) {
		LShr LShrInstruction = new LShr(getNonConflictName(name), lhs, rhs);
		insertInstruction(LShrInstruction);
		return LShrInstruction;
	}

	public Add buildAdd(Value lhs, Value rhs, String name) {
		Add addInst = new Add(getNonConflictName(name), lhs, rhs);
        insertInstruction(addInst);
        return addInst;
	}

	public Sub buildSub(Value lhs, Value rhs, String name) {
        Sub subInst = new Sub(getNonConflictName(name), lhs, rhs);
        insertInstruction(subInst);
        return subInst;
	}

	public Mul buildMul(Value lhs, Value rhs, String name) {
		Mul mulInst = new Mul(getNonConflictName(name), lhs, rhs);
		insertInstruction(mulInst);
		return mulInst;
	}

	public SDiv buildSDiv(Value lhs, Value rhs, String name) {
		SDiv sdivInst = new SDiv(getNonConflictName(name), lhs, rhs);
		insertInstruction(sdivInst);
		return sdivInst;
	}

	public SRem buildSRem(Value lhs, Value rhs, String name) {
		SRem sremInst = new SRem(getNonConflictName(name), lhs, rhs);
		insertInstruction(sremInst);
		return sremInst;
	}

	public ICmp buildICmp(Value op1, Value op2, String name, CompareOp code) {
		ICmp icmpInst = new ICmp(getNonConflictName(name), code, op1, op2);
		insertInstruction(icmpInst);
		return icmpInst;

	}

	public FAdd buildFAdd(Value lhs, Value rhs, String name) {
		FAdd addInst = new FAdd(getNonConflictName(name), lhs, rhs);
		insertInstruction(addInst);
		return addInst;
	}

	public FMul buildFMul(Value lhs, Value rhs, String name) {
		FMul mulInst = new FMul(getNonConflictName(name), lhs, rhs);
		insertInstruction(mulInst);
		return mulInst;
	}

	public FSub buildFSub(Value lhs, Value rhs, String name) {
		FSub subInst = new FSub(getNonConflictName(name), lhs, rhs);
		insertInstruction(subInst);
		return subInst;
	}

	public FDiv buildFDiv(Value lhs, Value rhs, String name) {
		FDiv fdivInst = new FDiv(getNonConflictName(name), lhs, rhs);
		insertInstruction(fdivInst);
		return fdivInst;
	}

	public FNeg buildFNeg(Value op, String name) {
		FNeg fnegInst = new FNeg(getNonConflictName(name), op);
		insertInstruction(fnegInst);
		return fnegInst;
	}

	public FCmp buildFCmp(Value lhs, Value rhs, String name, CompareOp code) {
		FCmp fcmpInst = new FCmp(getNonConflictName(name), code, lhs, rhs);
		insertInstruction(fcmpInst);
		return fcmpInst;
	}

	public FptoSi buildFptoSi(Value op, String name) {
		FptoSi fptoSiInst = new FptoSi(getNonConflictName(name), op);
		insertInstruction(fptoSiInst);
		return fptoSiInst;
	}

	public SitoFp buildSitoFp(Value op, String name) {
		SitoFp sitoFpInst = new SitoFp(getNonConflictName(name), op);
		insertInstruction(sitoFpInst);
		return sitoFpInst;
	}

	public Call buildCall(Function function, Value[] args, String name) {
		Call callInst = new Call(getNonConflictName(name), function, args);
		insertInstruction(callInst);
		return callInst;
	}

	public Alloca buildAlloca(Type type, String name) {
		Alloca allocaInst = new Alloca(getNonConflictName(name), type);
		insertInstruction(allocaInst);
		return allocaInst;
	}

	public Getptr buildGetptr(Value array, Value index, String name) {
		Getptr getptrInst = new Getptr(getNonConflictName(name), array, index);
		insertInstruction(getptrInst);
		return getptrInst;
	}

	public Load buildLoad(Value pointer, String name) {
		Load loadInst = new Load(getNonConflictName(name), pointer);
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
		assert currentBlock != null : "No current block to insert instruction into";
		currentBlock.insertInstruction(inst);
	}

	private String getNonConflictName(String originalName) {
		if (!occupiedNames.contains(originalName)) {
			occupiedNames.add(originalName);
			return originalName;
		}

		int suffix = 0;
		while (true) {
			String newName = originalName + suffix;
			if (!occupiedNames.contains(newName)) {
				occupiedNames.add(newName);
				return newName;
			}
			suffix += 1;
		}
	}
}