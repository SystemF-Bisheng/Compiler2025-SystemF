package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.type.interfaces.Atom;
import org.systemf.compiler.ir.type.interfaces.Type;

public enum I32 implements Atom {
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