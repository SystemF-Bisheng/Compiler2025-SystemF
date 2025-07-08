package org.systemf.compiler.ir;

import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyCompare;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyUnary;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.FpToSi;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.SiToFp;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
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
	protected T defaultValue() {
		return null;
	}

	public T visit(DummyBinary inst) {
		return defaultValue();
	}

	public T visit(DummyCompare inst) {
		return visit((DummyBinary) inst);
	}

	public T visit(DummyUnary inst) {
		return defaultValue();
	}

	@Override
	public T visit(Add inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(Sub inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(Mul inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(SDiv inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(SRem inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(ICmp inst) {
		return visit((DummyCompare) inst);
	}

	@Override
	public T visit(FAdd inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(FSub inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(FMul inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(FDiv inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(FNeg inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(FCmp inst) {
		return visit((DummyCompare) inst);
	}

	@Override
	public T visit(And inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(Or inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(Xor inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(Shl inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(LShr inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(AShr inst) {
		return visit((DummyBinary) inst);
	}

	@Override
	public T visit(FpToSi inst) {
		return visit((DummyUnary) inst);
	}

	@Override
	public T visit(SiToFp inst) {
		return visit((DummyUnary) inst);
	}

	public T visit(AbstractCall inst) {
		return defaultValue();
	}

	@Override
	public T visit(Call inst) {
		return visit((AbstractCall) inst);
	}

	@Override
	public T visit(CallVoid inst) {
		return visit((AbstractCall) inst);
	}

	@Override
	public T visit(Alloca inst) {
		return defaultValue();
	}

	@Override
	public T visit(GetPtr inst) {
		return defaultValue();
	}

	@Override
	public T visit(Load inst) {
		return defaultValue();
	}

	@Override
	public T visit(Store inst) {
		return defaultValue();
	}

	@Override
	public T visit(Unreachable inst) {
		return defaultValue();
	}

	@Override
	public T visit(Phi inst) {
		return defaultValue();
	}

	@Override
	public T visit(Br inst) {
		return defaultValue();
	}

	@Override
	public T visit(CondBr inst) {
		return defaultValue();
	}

	@Override
	public T visit(Ret inst) {
		return defaultValue();
	}

	@Override
	public T visit(RetVoid inst) {
		return defaultValue();
	}
}