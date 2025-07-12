package org.systemf.compiler.semantic.type;

public enum SysYInt implements SysYType {
	INT;

	@Override
	public boolean convertibleTo(SysYType other) {
		if (equals(other)) return true;
		return other == SysYFloat.FLOAT;
	}


	@Override
	public String toString() {
		return "int";
	}
}