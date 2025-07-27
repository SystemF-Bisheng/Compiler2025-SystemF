package org.systemf.compiler.ir.value.instruction.nonterminal;

import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;

public abstract class DummyIntBinary extends DummyBinary {
	protected DummyIntBinary(String name, Value x, Value y) {
		super(name, x, y, x.getType());
		TypeUtil.assertInteger(x.getType(), "Illegal x");
	}

	@Override
	public void setX(Value x) {
		TypeUtil.assertConvertible(x.getType(), type, "Illegal x");
		super.setX(x);
	}

	@Override
	public void setY(Value y) {
		TypeUtil.assertConvertible(y.getType(), type, "Illegal y");
		super.setY(y);
	}
}
