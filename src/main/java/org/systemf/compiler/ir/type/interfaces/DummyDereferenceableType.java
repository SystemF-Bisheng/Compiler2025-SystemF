package org.systemf.compiler.ir.type.interfaces;

import java.util.Objects;

public class DummyDereferenceableType extends DummyType implements Dereferenceable {
	protected final Type elementType;

	protected DummyDereferenceableType(String typeName, Type elementType) {
		super(typeName);
		this.elementType = elementType;
	}

	@Override
	public Type getElementType() {
		return elementType;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		DummyDereferenceableType that = (DummyDereferenceableType) o;
		return Objects.equals(elementType, that.elementType);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(elementType);
	}
}