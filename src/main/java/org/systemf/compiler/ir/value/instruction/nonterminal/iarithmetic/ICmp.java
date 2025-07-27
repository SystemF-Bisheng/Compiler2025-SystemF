package org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyCompare;

public class ICmp extends DummyCompare {
	public ICmp(String name, CompareOp method, Value x, Value y) {
		super(name, method, x, y, I32.INSTANCE);
	}

	@Override
	public String compareOperatorName() {
		return "icmp";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void setX(Value x) {
		TypeUtil.assertInteger(x.getType(), "Illegal x");
		super.setX(x);
	}

	@Override
	public void setY(Value y) {
		TypeUtil.assertInteger(y.getType(), "Illegal y");
		super.setY(y);
	}
}