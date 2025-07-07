package org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyCompare;

public class FCmp extends DummyCompare {
	public FCmp(String name, CompareOp method, Value x, Value y) {
		super(name, method, x, y, I32.INSTANCE);
	}

	@Override
	public String compareOperatorName() {
		return "fcmp";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}