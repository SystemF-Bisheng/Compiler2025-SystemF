package org.systemf.compiler.ir.type;

public enum I32 implements Type {
	INSTANCE;

	@Override
	public boolean convertibleTo(Type otherType) {
		return INSTANCE == otherType || Float.INSTANCE == otherType;
	}

	@Override
	public String getName() {
		return "i32";
	}
}