package org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.PotentialPositionSensitive;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;

public class FDiv extends DummyBinary implements PotentialPositionSensitive /* Divide by zero */ {
	public FDiv(String name, Value x, Value y) {
		super(name, x, y, Float.INSTANCE, Float.INSTANCE, Float.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "fdiv";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}