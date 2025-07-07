package org.systemf.compiler.ir.value.instruction.nonterminal.bitwise;

import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;

public class And extends DummyBinary {
	public And(String name, Value x, Value y) {
		super(name, x, y, I32.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "and";
	}
}