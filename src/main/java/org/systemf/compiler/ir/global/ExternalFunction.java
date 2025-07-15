package org.systemf.compiler.ir.global;

import org.systemf.compiler.ir.type.FunctionType;
import org.systemf.compiler.ir.value.DummyValue;

public class ExternalFunction extends DummyValue implements IFunction {
	final private String name;

	public ExternalFunction(String name, FunctionType type) {
		super(type);
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return String.format("import @%s: %s", name, type);
	}
}