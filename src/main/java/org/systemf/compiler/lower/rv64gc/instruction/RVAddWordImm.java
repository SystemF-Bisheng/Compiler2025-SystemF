package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyUnary;

public class RVAddWordImm extends DummyUnary {
	public long y;

	public RVAddWordImm(String name, Value x, long y) {
		super(name, x, I32.INSTANCE);
		this.y = y;
	}

	@Override
	public String operatorName() {
		return "addiw";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
