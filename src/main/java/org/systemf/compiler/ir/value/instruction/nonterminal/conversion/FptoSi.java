package org.systemf.compiler.ir.value.instruction.nonterminal.conversion;

import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.Util.ValueUtil;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

public class FptoSi extends DummyNonTerminal {
	public final Value op;

	public FptoSi(String name, Value op) {
		super(I32.INSTANCE, name);
		this.op = op;
	}

	@Override
	public String toString() {
		return String.format("%%%s = fptosi %%%s", name, ValueUtil.getValueName(op));
	}
}