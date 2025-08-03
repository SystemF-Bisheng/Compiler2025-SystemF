package org.systemf.compiler.lower.rv64gc.module.position;

import org.systemf.compiler.lower.rv64gc.module.register.RVRegisterType;

public record RVRegister(RVRegisterType type, int index) implements RVPosition, Comparable<RVRegister> {
	@Override
	public String toString() {
		return type.prefix + index;
	}

	@Override
	public int compareTo(RVRegister other) {
		if (type != other.type) return type.compareTo(other.type);
		return Integer.compare(index, other.index);
	}
}
