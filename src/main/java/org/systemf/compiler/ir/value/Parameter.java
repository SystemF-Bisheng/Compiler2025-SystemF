package org.systemf.compiler.ir.value;

import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.type.interfaces.Type;

public class Parameter extends DummyValue implements INamed {
	protected final String name;

	public Parameter(Type type, String name) {
		super(type);
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
