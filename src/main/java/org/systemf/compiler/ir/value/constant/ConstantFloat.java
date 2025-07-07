package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.Float;

public class ConstantFloat extends DummyConstant {
	final public double value;

	public ConstantFloat(double value) {
		super(Float.INSTANCE);
		this.value = value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}