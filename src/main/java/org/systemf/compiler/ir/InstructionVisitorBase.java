package org.systemf.compiler.ir;

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
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Unreachable;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.ir.value.instruction.terminal.Ret;
import org.systemf.compiler.ir.value.instruction.terminal.RetVoid;

public class InstructionVisitorBase<T> implements InstructionVisitor<T> {

	@Override
	public T visit(Add inst) {
		return null;
	}

	@Override
	public T visit(Sub inst) {
		return null;
	}

	@Override
	public T visit(Mul inst) {
		return null;
	}

	@Override
	public T visit(SDiv inst) {
		return null;
	}

	@Override
	public T visit(SRem inst) {
		return null;
	}

	@Override
	public T visit(ICmp inst) {
		return null;
	}

	@Override
	public T visit(FAdd inst) {
		return null;
	}

	@Override
	public T visit(FSub inst) {
		return null;
	}

	@Override
	public T visit(FMul inst) {
		return null;
	}

	@Override
	public T visit(FDiv inst) {
		return null;
	}

	@Override
	public T visit(FNeg inst) {
		return null;
	}

	@Override
	public T visit(FCmp inst) {
		return null;
	}

	@Override
	public T visit(And inst) {
		return null;
	}

	@Override
	public T visit(Or inst) {
		return null;
	}

	@Override
	public T visit(Xor inst) {
		return null;
	}

	@Override
	public T visit(Shl inst) {
		return null;
	}

	@Override
	public T visit(LShr inst) {
		return null;
	}

	@Override
	public T visit(AShr inst) {
		return null;
	}

	@Override
	public T visit(FpToSi inst) {
		return null;
	}

	@Override
	public T visit(SiToFp inst) {
		return null;
	}

	@Override
	public T visit(Call inst) {
		return null;
	}

	@Override
	public T visit(CallVoid inst) {
		return null;
	}

	@Override
	public T visit(Alloca inst) {
		return null;
	}

	@Override
	public T visit(GetPtr inst) {
		return null;
	}

	@Override
	public T visit(Load inst) {
		return null;
	}

	@Override
	public T visit(Store inst) {
		return null;
	}

	@Override
	public T visit(Unreachable inst) {
		return null;
	}

	@Override
	public T visit(Phi inst) {
		return null;
	}

	@Override
	public T visit(Br inst) {
		return null;
	}

	@Override
	public T visit(CondBr inst) {
		return null;
	}

	@Override
	public T visit(Ret inst) {
		return null;
	}

	@Override
	public T visit(RetVoid inst) {
		return null;
	}
}
