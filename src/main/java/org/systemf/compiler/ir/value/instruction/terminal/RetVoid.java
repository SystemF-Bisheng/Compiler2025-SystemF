package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.InstructionVisitor;

public class RetVoid extends DummyTerminal {
	public RetVoid() {
	}

	@Override
	public String toString() {
		return "ret void";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}