package org.systemf.compiler.semantic.type;

public interface SysYType {
	default boolean convertibleTo(SysYType other) {
		return equals(other);
	}
}