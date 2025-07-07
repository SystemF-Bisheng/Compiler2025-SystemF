package org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;

public class FMul extends DummyBinary {
	public FMul(String name, Value x, Value y) {
		super(name, x, y, Float.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "fmul";
	}
}