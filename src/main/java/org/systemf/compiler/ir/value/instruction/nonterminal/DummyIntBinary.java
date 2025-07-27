package org.systemf.compiler.ir.value.instruction.nonterminal;

import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;

public abstract class DummyIntBinary extends DummyBinary {
	protected DummyIntBinary(String name, Value x, Value y, Type resultType) {
		super(name, x, y, resultType);
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
