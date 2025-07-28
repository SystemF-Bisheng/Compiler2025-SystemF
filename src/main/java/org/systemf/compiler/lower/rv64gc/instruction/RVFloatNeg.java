package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyUnary;

public class RVFloatNeg extends DummyUnary {
	public RVFloatNeg(String name, Value x) {
		super(name, x, Float.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "fneg.s";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
