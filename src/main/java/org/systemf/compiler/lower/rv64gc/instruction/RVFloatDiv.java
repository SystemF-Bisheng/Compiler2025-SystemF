package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;

public class RVFloatDiv extends DummyBinary {
	protected RVFloatDiv(String name, Value x, Value y) {
		super(name, x, y, Float.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "fdiv.s";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
