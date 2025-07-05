package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.type.interfaces.Type;

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