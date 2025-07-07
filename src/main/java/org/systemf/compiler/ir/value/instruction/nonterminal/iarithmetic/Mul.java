package org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic;

import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.Util.ValueUtil;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

public class Mul extends DummyNonTerminal {
	public final Value op1, op2;

	public Mul(String name, Value op1, Value op2) {
		super(I32.INSTANCE, name);
		this.op1 = op1;
		this.op2 = op2;
	}

	@Override
	public String toString() {
		return String.format("%%%s = mul %%%s, %%%s", name, ValueUtil.getValueName(op1), ValueUtil.getValueName(op2));
	}
}