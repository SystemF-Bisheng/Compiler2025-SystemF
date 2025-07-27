package org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyCompare;

public class FCmp extends DummyCompare {
	public FCmp(String name, CompareOp method, Value x, Value y) {
		super(name, method, x, y);
	}

	@Override
	public String compareOperatorName() {
		return "fcmp";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void setX(Value x) {
		TypeUtil.assertConvertible(x.getType(), Float.INSTANCE, "Illegal x");
		super.setX(x);
	}

	@Override
	public void setY(Value y) {
		TypeUtil.assertConvertible(y.getType(), Float.INSTANCE, "Illegal y");
		super.setY(y);
	}
}