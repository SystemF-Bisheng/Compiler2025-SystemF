package org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;

public class SRem extends DummyBinary {
	public SRem(String name, Value x, Value y) {
		super(name, x, y, I32.INSTANCE, I32.INSTANCE, I32.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "srem";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}