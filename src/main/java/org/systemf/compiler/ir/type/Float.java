package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.type.interfaces.Atom;
import org.systemf.compiler.ir.type.interfaces.Type;

public enum Float implements Atom {
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