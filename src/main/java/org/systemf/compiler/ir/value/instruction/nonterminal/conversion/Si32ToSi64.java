package org.systemf.compiler.ir.value.instruction.nonterminal.conversion;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.type.I64;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyFixedUnary;

public class Si32ToSi64 extends DummyFixedUnary {
	public Si32ToSi64(String name, Value x) {
		super(name, x, I32.INSTANCE, I64.INSTANCE);
	}

	@Override
	public String operatorName() {
		return "si32tosi64";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
