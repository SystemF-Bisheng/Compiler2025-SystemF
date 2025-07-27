package org.systemf.compiler.ir.value.instruction.nonterminal.bitwise;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyIntBinary;

public class AShr extends DummyIntBinary {
	public AShr(String name, Value x, Value y) {
		super(name, x, y);
	}

	@Override
	public String operatorName() {
		return "ashr";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}