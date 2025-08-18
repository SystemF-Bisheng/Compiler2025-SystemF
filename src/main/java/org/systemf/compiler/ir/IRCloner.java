package org.systemf.compiler.ir;

import org.systemf.compiler.ir.value.instruction.Instruction;
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
import org.systemf.compiler.ir.value.instruction.terminal.*;

public class IRCloner extends InstructionVisitorBase<Instruction> {
	public final IRBuilder builder;

	public IRCloner(IRBuilder builder) {
		this.builder = builder;
	}

	@Override
	protected Instruction defaultValue() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Instruction visit(Add inst) {
		return builder.buildAdd(inst.getX(), inst.getY(), inst.getName());
	}

	@Override
	public Instruction visit(Sub inst) {
		return builder.buildSub(inst.getX(), inst.getY(), inst.getName());
	}

	@Override
	public Instruction visit(Mul inst) {
		return builder.buildMul(inst.getX(), inst.getY(), inst.getName());
	}

	@Override
	public Instruction visit(SDiv inst) {
		return builder.buildSDiv(inst.getX(), inst.getY(), inst.getName());
	}

	@Override
	public Instruction visit(SRem inst) {
		return builder.buildSRem(inst.getX(), inst.getY(), inst.getName());
	}

	@Override
	public Instruction visit(ICmp inst) {
		return builder.buildICmp(inst.getX(), inst.getY(), inst.getName(), inst.method);
	}

	@Override
	public Instruction visit(FAdd inst) {
		return builder.buildFAdd(inst.getX(), inst.getY(), inst.getName());
	}

	@Override
	public Instruction visit(FSub inst) {
		return builder.buildFSub(inst.getX(), inst.getY(), inst.getName());
	}

	@Override
	public Instruction visit(FMul inst) {
		return builder.buildFMul(inst.getX(), inst.getY(), inst.getName());
	}

	@Override
	public Instruction visit(FDiv inst) {
		return builder.buildFDiv(inst.getX(), inst.getY(), inst.getName());
	}

	@Override
	public Instruction visit(FNeg inst) {
		return builder.buildFNeg(inst.getX(), inst.getName());
	}

	@Override
	public Instruction visit(FCmp inst) {
		return builder.buildFCmp(inst.getX(), inst.getY(), inst.getName(), inst.method);
	}

	@Override
	public Instruction visit(FMulAdd inst) {
		return builder.buildFMulAdd(inst.getX(), inst.getY(), inst.getZ(), inst.getName());
	}

	@Override
	public Instruction visit(FMulSub inst) {
		return builder.buildFMulSub(inst.getX(), inst.getY(), inst.getZ(), inst.getName());
	}

	@Override
	public Instruction visit(FNegMulAdd inst) {
		return builder.buildFNegMulAdd(inst.getX(), inst.getY(), inst.getZ(), inst.getName());
	}

	@Override
	public Instruction visit(FNegMulSub inst) {
		return builder.buildFNegMulSub(inst.getX(), inst.getY(), inst.getZ(), inst.getName());
	}

	@Override
	public Instruction visit(And inst) {
		return builder.buildAnd(inst.getX(), inst.getY(), inst.getName());
	}

	@Override
	public Instruction visit(Or inst) {
		return builder.buildOr(inst.getX(), inst.getY(), inst.getName());
	}

	@Override
	public Instruction visit(Xor inst) {
		return builder.buildXor(inst.getX(), inst.getY(), inst.getName());
	}

	@Override
	public Instruction visit(Shl inst) {
		return builder.buildShl(inst.getX(), inst.getY(), inst.getName());
	}

	@Override
	public Instruction visit(LShr inst) {
		return builder.buildLShr(inst.getX(), inst.getY(), inst.getName());
	}

	@Override
	public Instruction visit(AShr inst) {
		return builder.buildAShr(inst.getX(), inst.getY(), inst.getName());
	}

	@Override
	public Instruction visit(PtrCast inst) {
		return builder.buildPtrCast(inst.getX(), inst.getType(), inst.getName());
	}

	@Override
	public Instruction visit(FpToSi32 inst) {
		return builder.buildFpToSi32(inst.getX(), inst.getName());
	}

	@Override
	public Instruction visit(Si32ToFp inst) {
		return builder.buildSi32ToFp(inst.getX(), inst.getName());
	}

	@Override
	public Instruction visit(Si32ToSi64 inst) {
		return builder.buildSi32ToSi64(inst.getX(), inst.getName());
	}

	@Override
	public Instruction visit(Si64ToSi32 inst) {
		return builder.buildSi64ToSi32(inst.getX(), inst.getName());
	}

	@Override
	public Instruction visit(Call inst) {
		return builder.buildCall(inst.getFunction(), inst.getName(), inst.getArgs());
	}

	@Override
	public Instruction visit(CallVoid inst) {
		return builder.buildCallVoid(inst.getFunction(), inst.getArgs());
	}

	@Override
	public Instruction visit(Alloca inst) {
		return builder.buildAlloca(inst.valueType, inst.getName());
	}

	@Override
	public Instruction visit(GetPtr inst) {
		return builder.buildGetPtr(inst.getArrayPtr(), inst.getIndex(), inst.getName());
	}

	@Override
	public Instruction visit(Load inst) {
		return builder.buildLoad(inst.getPointer(), inst.getName());
	}

	@Override
	public Instruction visit(Store inst) {
		return builder.buildStore(inst.getSrc(), inst.getDest());
	}

	@Override
	public Instruction visit(Unreachable inst) {
		return builder.buildUnreachable();
	}

	@Override
	public Instruction visit(Phi inst) {
		var phi = builder.buildPhi(inst.getType(), inst.getName());
		inst.getIncoming().forEach(phi::addIncoming);
		return phi;
	}

	@Override
	public Instruction visit(Br inst) {
		return builder.buildBr(inst.getTarget());
	}

	@Override
	public Instruction visit(CondBr inst) {
		return builder.buildCondBr(inst.getCondition(), inst.getTrueTarget(), inst.getFalseTarget());
	}

	@Override
	public Instruction visit(Ret inst) {
		return builder.buildRet(inst.getReturnValue());
	}

	@Override
	public Instruction visit(RetVoid inst) {
		return builder.buildRetVoid();
	}
}
