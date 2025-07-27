package org.systemf.compiler.ir.value.instruction.nonterminal;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;

public abstract class DummyFloatBinary extends DummyBinary {
	protected DummyFloatBinary(String name, Value x, Value y, Type resultType) {
		super(name, x, y, resultType);
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
