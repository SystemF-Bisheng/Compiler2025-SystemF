package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.I64;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyUnary;

public class RVAddImm extends DummyUnary {
	public long y;

	public RVAddImm(String name, Value x, long y) {
		super(name, x, I64.INSTANCE);
		this.y = y;
	}

	@Override
	public String operatorName() {
		return "addi";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
