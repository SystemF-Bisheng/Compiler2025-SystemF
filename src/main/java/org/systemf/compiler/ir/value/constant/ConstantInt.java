package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.I32;

public class ConstantInt extends DummyConstant {
	final public long value;

	public ConstantInt(long value) {
		super(I32.INSTANCE);
		this.value = value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}