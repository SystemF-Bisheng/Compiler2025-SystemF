package org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic;

import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;

public class Mul extends DummyBinary {
	public Mul(String name, Value x, Value y) {
		super(name, x, y, I32.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "mul";
	}
}