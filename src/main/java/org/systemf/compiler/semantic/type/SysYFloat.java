package org.systemf.compiler.semantic.type;

public enum SysYFloat implements SysYType {
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
}