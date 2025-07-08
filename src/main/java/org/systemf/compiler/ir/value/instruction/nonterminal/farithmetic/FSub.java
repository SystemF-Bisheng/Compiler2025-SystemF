package org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;

public class FSub extends DummyBinary {
	public FSub(String name, Value x, Value y) {
		super(name, x, y, Float.INSTANCE, Float.INSTANCE, Float.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "fsub";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}