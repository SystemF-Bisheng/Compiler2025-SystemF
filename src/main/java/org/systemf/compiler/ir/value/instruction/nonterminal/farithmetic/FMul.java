package org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Util.ValueUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;

public class FMul extends DummyValueNonTerminal {
	public final Value op1, op2;

	public FMul(String name, Value op1, Value op2) {
		super(Float.INSTANCE, name);
		this.op1 = op1;
		this.op2 = op2;
	}

	@Override
	public String toString() {
		return String.format("%%%s = fmul %%%s, %%%s", name, ValueUtil.getValueName(op1), ValueUtil.getValueName(op2));
	}
}