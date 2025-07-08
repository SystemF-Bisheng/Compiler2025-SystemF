package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.interfaces.Type;

public enum I32 implements Type, Sized {
	INSTANCE;

	@Override
	public boolean convertibleTo(Type otherType) {
		return INSTANCE == otherType;
	}

	@Override
	public String getName() {
		return "i32";
	}

	@Override
	public String toString() {
		return getName();
	}
}