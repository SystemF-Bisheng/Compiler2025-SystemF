package org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic;

import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

public class SDiv extends DummyValueNonTerminal {
	public final Value op1, op2;

	public SDiv(String name, Value op1, Value op2) {
		super(I32.INSTANCE, name);
		this.op1 = op1;
		this.op2 = op2;
	}

	@Override
	public String toString() {
		return String.format("%%%s = sdiv %%%s, %%%s", name, ValueUtil.getValueName(op1), ValueUtil.getValueName(op2));
	}
}