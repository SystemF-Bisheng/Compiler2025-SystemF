package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.Float;

public class ConstantFloat extends Constant {
	final public double value;

	public ConstantFloat(double value) {
		super(Float.getInstance(), Double.toString(value));
		this.value = value;
	}

	@Override
	public double getConstantFloatValue() {
		return value;
	}
}