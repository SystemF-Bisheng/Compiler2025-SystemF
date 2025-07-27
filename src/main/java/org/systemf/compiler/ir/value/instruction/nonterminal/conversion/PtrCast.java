package org.systemf.compiler.ir.value.instruction.nonterminal.conversion;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyUnary;

public class PtrCast extends DummyUnary {
	public PtrCast(String name, Value x, Type resultType) {
		super(name, x, resultType);
	}

	@Override
	public String operatorName() {
		return "ptrcast " + type;
	}

	public void setX(Value x) {
		var xType = x.getType();
		if (!(xType instanceof Pointer)) throw new IllegalArgumentException("x is not a pointer");
		TypeUtil.assertConvertible(xType, type, "Illegal x");
		super.setX(x);
	}

	@Override
	public boolean contentEqual(Value other) {
		if (!super.contentEqual(other)) return false;
		return type.equals(((PtrCast) other).type);
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
