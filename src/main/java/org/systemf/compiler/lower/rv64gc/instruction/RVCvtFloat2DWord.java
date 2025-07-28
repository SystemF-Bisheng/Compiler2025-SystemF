package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.I64;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyUnary;

public class RVCvtFloat2DWord extends DummyUnary {
	public RVCvtFloat2DWord(String name, Value x) {
		super(name, x, I64.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "fcvt.l.s";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
