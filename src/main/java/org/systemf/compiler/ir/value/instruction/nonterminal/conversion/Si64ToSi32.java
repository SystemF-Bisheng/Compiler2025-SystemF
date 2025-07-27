package org.systemf.compiler.ir.value.instruction.nonterminal.conversion;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.type.I64;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyUnary;

public class Si64ToSi32 extends DummyUnary {
	public Si64ToSi32(String name, Value x) {
		super(name, x, I64.INSTANCE, I32.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "si64tosi32";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
