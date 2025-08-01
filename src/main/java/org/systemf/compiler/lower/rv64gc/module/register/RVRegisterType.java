package org.systemf.compiler.lower.rv64gc.module.register;

public enum RVRegisterType {
	INTEGER("x"), FLOAT("f");

	public final String prefix;

	RVRegisterType(String prefix) {
		this.prefix = prefix;
	}
}
