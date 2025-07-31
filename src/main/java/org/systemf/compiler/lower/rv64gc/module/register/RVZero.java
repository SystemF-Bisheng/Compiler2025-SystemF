package org.systemf.compiler.lower.rv64gc.module.register;

import org.systemf.compiler.ir.type.I64;
import org.systemf.compiler.ir.value.DummyValue;

public class RVZero extends DummyValue {
	public static final RVZero INSTANCE = new RVZero();

	private RVZero() {
		super(I64.INSTANCE);
	}

	@Override
	public String toString() {
		return "zero";
	}
}
