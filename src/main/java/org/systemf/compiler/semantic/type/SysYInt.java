package org.systemf.compiler.semantic.type;

public enum SysYInt implements SysYType, SysYNumeric {
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

	@Override
	public boolean compare(SysYNumeric other) {
		return other != INT;
	}
}