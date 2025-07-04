package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.I32;

public class ConstantInt extends Constant {
	final public long value;

	public ConstantInt(long value) {
		super(I32.getInstance(), Long.toString(value));
		this.value = value;
	}

	@Override
	public long getConstantIntValue() {
		return value;
	}
}