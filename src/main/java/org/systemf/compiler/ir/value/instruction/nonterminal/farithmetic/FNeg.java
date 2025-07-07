package org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

public class FNeg extends DummyValueNonTerminal {
	public final Value op;

	public FNeg(String name, Value op) {
		super(Float.INSTANCE, name);
		this.op = op;
	}

	@Override
	public String toString() {
		return String.format("%%%s = fneg %%%s, %%%s", name, ValueUtil.getValueName(op));
	}
}