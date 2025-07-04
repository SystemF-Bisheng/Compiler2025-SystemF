package org.systemf.compiler.ir.type;

public enum Void implements Type {
	INSTANCE;

	@Override
	public boolean convertibleTo(Type otherType) {
		return INSTANCE == otherType;
	}

	@Override
	public String getName() {
		return "void";
	}
}