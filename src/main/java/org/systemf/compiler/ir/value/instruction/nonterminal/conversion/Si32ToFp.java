package org.systemf.compiler.ir.value.instruction.nonterminal.conversion;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyFixedUnary;

public class Si32ToFp extends DummyFixedUnary {
	public Si32ToFp(String name, Value x) {
		super(name, x, I32.INSTANCE, Float.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "si32tofp";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}