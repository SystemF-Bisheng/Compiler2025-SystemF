package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.I64;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;

public class RVFloatEq extends DummyBinary {
	public RVFloatEq(String name, Value x, Value y) {
		super(name, x, y, I64.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "feq.s";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
