package org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.Util.ValueUtil;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

public class FNeg extends DummyNonTerminal {
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