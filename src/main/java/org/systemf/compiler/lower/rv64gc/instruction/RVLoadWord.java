package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;

public class RVLoadWord extends RVLoad {
	public RVLoadWord(String name, Value ptr, long offset) {
		super(name, I32.INSTANCE, ptr, offset);
	}

	@Override
	protected String operatorName() {
		return "lw";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
