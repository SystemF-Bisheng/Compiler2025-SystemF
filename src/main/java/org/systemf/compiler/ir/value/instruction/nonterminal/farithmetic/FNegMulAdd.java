package org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyFloatTriple;

/**
 * -x * y - z
 */
public class FNegMulAdd extends DummyFloatTriple {
	public FNegMulAdd(String name, Value x, Value y, Value z) {
		super(name, x, y, z);
	}

	@Override
	public String operatorName() {
		return "fnma";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
