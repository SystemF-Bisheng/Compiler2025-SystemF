package org.systemf.compiler.ir.value.instruction.nonterminal.conversion;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Util.ValueUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;

public class SiToFp extends DummyValueNonTerminal {
	public final Value op;

	public SiToFp(String name, Value op) {
		super(Float.INSTANCE, name);
		this.op = op;
	}

	@Override
	public String toString() {
		return String.format("%%%s = sitofp %%%s", name, ValueUtil.getValueName(op));
	}
}