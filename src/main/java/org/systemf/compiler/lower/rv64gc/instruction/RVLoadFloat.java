package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;

public class RVLoadFloat extends RVLoad {
	protected RVLoadFloat(String name, Value ptr, long offset) {
		super(name, Float.INSTANCE, ptr, offset);
	}

	@Override
	protected String operatorName() {
		return "flw";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
