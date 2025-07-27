package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyUnary;

public class RVCvtDWord2Float extends DummyUnary {
	protected RVCvtDWord2Float(String name, Value x) {
		super(name, x, Float.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "fcvt.s.l";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
