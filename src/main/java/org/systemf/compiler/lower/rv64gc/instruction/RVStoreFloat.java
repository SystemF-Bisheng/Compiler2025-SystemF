package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.value.Value;

public class RVStoreFloat extends RVStore {
	public RVStoreFloat(Value src, Value dest, long offset) {
		super(src, dest, offset);
	}

	@Override
	protected String operatorName() {
		return "fsw";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
