package org.systemf.compiler.ir.type;

import java.util.Objects;

public class Pointer extends DummyType {
	final public Type elementType;

	public Pointer(Type elementType) {
		super(String.format("%s*", elementType.toString()));
		this.elementType = elementType;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pointer pointer)) return false;
		return Objects.equals(elementType, pointer.elementType);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(elementType);
	}
}