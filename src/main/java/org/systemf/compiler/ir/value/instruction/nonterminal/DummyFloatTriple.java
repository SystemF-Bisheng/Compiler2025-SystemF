package org.systemf.compiler.ir.value.instruction.nonterminal;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;

public abstract class DummyFloatTriple extends DummyTriple {
	protected DummyFloatTriple(String name, Value x, Value y, Value z) {
		super(name, x, y, z, Float.INSTANCE);
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

	@Override
	public void setZ(Value z) {
		TypeUtil.assertConvertible(z.getType(), Float.INSTANCE, "Illegal z");
		super.setZ(z);
	}
}
