package org.systemf.compiler.semantic.type;

public enum SysYFloat implements SysYType, SysYNumeric {
	FLOAT;

	@Override
	public boolean convertibleTo(SysYType other) {
		if (equals(other)) return true;
		return other == SysYInt.INT;
	}

	@Override
	public String toString() {
		return "float";
	}

	@Override
	public boolean compare(SysYNumeric other) {
		return false;
	}
}