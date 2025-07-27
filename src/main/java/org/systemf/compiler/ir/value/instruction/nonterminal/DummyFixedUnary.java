package org.systemf.compiler.ir.value.instruction.nonterminal;

import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;

public abstract class DummyFixedUnary extends DummyUnary {
	private final Type xType;

	protected DummyFixedUnary(String name, Value x, Type xType, Type resultType) {
		super(name, x, resultType);
		TypeUtil.assertConvertible(x.getType(), xType, "Illegal x");
		this.xType = xType;
	}

	@Override
	public void setX(Value x) {
		if (xType != null) TypeUtil.assertConvertible(x.getType(), xType, "Illegal x");
		super.setX(x);
	}
}
