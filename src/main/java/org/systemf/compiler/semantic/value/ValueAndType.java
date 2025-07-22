package org.systemf.compiler.semantic.value;

import org.systemf.compiler.semantic.type.SysYType;

public record ValueAndType(ValueClass valueClass, SysYType type) {
	public static ValueAndType ofLeft(SysYType type) {
		return new ValueAndType(ValueClass.LEFT, type);
	}

	public static ValueAndType ofRight(SysYType type) {
		return new ValueAndType(ValueClass.RIGHT, type);
	}

	public boolean convertibleTo(ValueAndType other) {
		return valueClass.convertibleTo(other.valueClass) && type.convertibleTo(other.type);
	}

	@Override
	public String toString() {
		return valueClass.toString() + " " + type.toString();
	}
}