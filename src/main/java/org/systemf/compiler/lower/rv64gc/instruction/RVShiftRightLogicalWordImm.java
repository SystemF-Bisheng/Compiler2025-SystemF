package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyUnary;

public class RVShiftRightLogicalWordImm extends DummyUnary {
	public long y;

	public RVShiftRightLogicalWordImm(String name, Value x, long y) {
		super(name, x, I32.INSTANCE);
		this.y = y;
	}

	@Override
	public String operatorName() {
		return "srliw";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
