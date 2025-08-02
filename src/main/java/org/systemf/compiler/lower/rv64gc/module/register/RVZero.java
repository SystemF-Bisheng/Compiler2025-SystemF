package org.systemf.compiler.lower.rv64gc.module.register;

import org.systemf.compiler.ir.type.I64;
import org.systemf.compiler.lower.rv64gc.module.position.RVRegister;

public class RVZero extends RVBuiltInRegister {
	public static final RVZero INSTANCE = new RVZero();

	private RVZero() {
		super(I64.INSTANCE);
	}

	@Override
	public String toString() {
		return "zero";
	}

	@Override
	public RVRegister position() {
		return new RVRegister(RVRegisterType.INTEGER, 0);
	}
}
