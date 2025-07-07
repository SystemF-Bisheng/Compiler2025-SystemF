package org.systemf.compiler.ir.value.instruction.nonterminal.conversion;

import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyUnary;

public class FpToSi extends DummyUnary {
	public FpToSi(String name, Value x) {
		super(name, x, I32.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "fptosi";
	}
}