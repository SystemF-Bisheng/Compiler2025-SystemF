package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.interfaces.Type;

public enum Float implements Type, Sized {
	INSTANCE;

	@Override
	public boolean convertibleTo(Type otherType) {
		return otherType == INSTANCE;
	}

	@Override
	public String getName() {
		return "float";
	}

	@Override
	public String toString() {
		return getName();
	}
}