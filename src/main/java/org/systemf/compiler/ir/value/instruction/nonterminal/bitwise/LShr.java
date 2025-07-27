package org.systemf.compiler.ir.value.instruction.nonterminal.bitwise;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyIntBinary;

public class LShr extends DummyIntBinary {
	public LShr(String name, Value x, Value y) {
		super(name, x, y);
	}

	@Override
	public String operatorName() {
		return "lshr";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}