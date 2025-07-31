package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;

public class RVShiftLeftWordImm extends RVImmBinary {
	public RVShiftLeftWordImm(String name, Value x, long y) {
		super(name, x, y, I32.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "slliw";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
