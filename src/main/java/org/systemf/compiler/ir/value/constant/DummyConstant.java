package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.DummyValue;
import org.systemf.compiler.ir.value.Value;

public abstract class DummyConstant extends DummyValue implements Constant {
	protected DummyConstant(Type type) {
		super(type);
	}

	@Override
	public boolean contentEqual(Value other) {
		if (getClass() != other.getClass()) return false;
		return type.equals(other.getType());
	}
}