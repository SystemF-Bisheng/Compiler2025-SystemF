package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyUnary;

public class RVCvtFloat2Word extends DummyUnary {
	protected RVCvtFloat2Word(String name, Value x) {
		super(name, x, I32.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "fcvt.w.s";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
