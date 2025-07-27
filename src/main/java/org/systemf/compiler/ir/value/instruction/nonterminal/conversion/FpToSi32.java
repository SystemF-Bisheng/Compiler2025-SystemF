package org.systemf.compiler.ir.value.instruction.nonterminal.conversion;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyUnary;

public class FpToSi32 extends DummyUnary {
	public FpToSi32(String name, Value x) {
		super(name, x, Float.INSTANCE, I32.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "fptosi32";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}