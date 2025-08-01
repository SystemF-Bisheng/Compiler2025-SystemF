package org.systemf.compiler.lower.rv64gc.module.position;

import org.systemf.compiler.lower.rv64gc.module.register.RVRegisterType;

public record RVRegister(RVRegisterType type, int index) implements RVPosition {
	@Override
	public String toString() {
		return type.prefix + index;
	}
}
