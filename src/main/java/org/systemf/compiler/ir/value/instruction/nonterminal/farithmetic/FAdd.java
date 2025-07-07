package org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;

public class FAdd extends DummyBinary {
	public FAdd(String name, Value x, Value y) {
		super(name, x, y, Float.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "fadd";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}