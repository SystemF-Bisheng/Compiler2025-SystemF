package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.I64;
import org.systemf.compiler.ir.value.Value;

public class RVLoadDWord extends RVLoad {
	public RVLoadDWord(String name, Value ptr, long offset) {
		super(name, I64.INSTANCE, ptr, offset);
	}

	@Override
	protected String operatorName() {
		return "ld";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
