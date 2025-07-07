package org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyUnary;

public class FNeg extends DummyUnary {
	public FNeg(String name, Value x) {
		super(name, x, Float.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "fneg";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}