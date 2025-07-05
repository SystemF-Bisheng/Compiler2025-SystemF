package org.systemf.compiler.ir.type.interfaces;

import java.util.Objects;

public class DummyIndexableType extends DummyType implements Indexable {
	protected final Sized elementType;

	protected DummyIndexableType(String typeName, Sized elementType) {
		super(typeName);
		this.elementType = elementType;
	}

	@Override
	public Sized getElementType() {
		return elementType;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		DummyIndexableType that = (DummyIndexableType) o;
		return Objects.equals(elementType, that.elementType);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(elementType);
	}
}