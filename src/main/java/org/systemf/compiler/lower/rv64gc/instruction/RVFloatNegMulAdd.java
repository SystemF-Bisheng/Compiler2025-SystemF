package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyTriple;

/**
 * -(x * y + z) = -x * y - z
 */
public class RVFloatNegMulAdd extends DummyTriple {
	public RVFloatNegMulAdd(String name, Value x, Value y, Value z) {
		super(name, x, y, z, Float.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "fnmadd.s";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
