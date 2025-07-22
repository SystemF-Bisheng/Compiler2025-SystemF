package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;

public class ConstantFloat extends DummyConstant {
	private static final ConstantFloat ZERO = new ConstantFloat(0);
	final public double value;

	private ConstantFloat(double value) {
		super(Float.INSTANCE);
		this.value = value;
	}

	public static ConstantFloat valueOf(double value) {
		if (value == 0) return ZERO;
		return new ConstantFloat(value);
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public boolean contentEqual(Value other) {
		if (!super.contentEqual(other)) return false;
		var otherFloat = (ConstantFloat) other;
		return (float) value == (float) otherFloat.value;
	}
}