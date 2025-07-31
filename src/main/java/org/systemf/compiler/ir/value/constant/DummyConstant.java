package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.value.DummyValue;
import org.systemf.compiler.ir.value.Value;

public abstract class DummyConstant extends DummyValue implements Constant {
	private final Sized sizedType;

	protected DummyConstant(Sized type) {
		super(type);
		this.sizedType = type;
	}

	@Override
	public boolean contentEqual(Value other) {
		if (getClass() != other.getClass()) return false;
		return type.equals(other.getType());
	}

	@Override
	public Sized getType() {
		return sizedType;
	}
}