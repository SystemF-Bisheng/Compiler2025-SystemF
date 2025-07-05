package org.systemf.compiler.ir.type;

import java.util.Objects;

public class Pointer extends DummyIndexableType {
	public Pointer(Type elementType) {
		super(String.format("%s*", elementType.toString()), elementType);
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