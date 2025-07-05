package org.systemf.compiler.ir.value;

import org.systemf.compiler.ir.type.interfaces.Type;

public abstract class DummyValue implements Value {
	final protected Type type;

	protected DummyValue(Type type) {
		this.type = type;
	}

	@Override
	public Type getType() {
		return type;
	}
}