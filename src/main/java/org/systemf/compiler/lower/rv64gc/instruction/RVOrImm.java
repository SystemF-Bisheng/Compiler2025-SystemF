package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.I64;
import org.systemf.compiler.ir.value.Value;

public class RVOrImm extends RVImmBinary {
	public RVOrImm(String name, Value x, long y) {
		super(name, x, y, I64.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "ori";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
