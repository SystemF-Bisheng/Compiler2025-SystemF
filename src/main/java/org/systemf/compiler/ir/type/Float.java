package org.systemf.compiler.ir.type;

public enum Float implements Type {
	INSTANCE;

	@Override
	public boolean convertibleTo(Type otherType) {
		return otherType == INSTANCE;
	}

	@Override
	public String getName() {
		return "float";
	}
}