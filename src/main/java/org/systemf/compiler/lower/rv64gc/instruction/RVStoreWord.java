package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.value.Value;

public class RVStoreWord extends RVStore {
	public RVStoreWord(Value src, Value dest) {
		super(src, dest);
	}

	@Override
	protected String operatorName() {
		return "sw";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
