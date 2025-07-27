package org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyIntBinary;

public class Add extends DummyIntBinary {
	public Add(String name, Value x, Value y) {
		super(name, x, y);
	}

	@Override
	public String operatorName() {
		return "add";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}