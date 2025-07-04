package org.systemf.compiler.ir.type;

import java.util.Objects;

public class Array extends DummyType {
	final public Type elementType;
	final public int length;

	public Array(int length, Type elementType) {
		super(String.format("[%d x %s]", length, elementType.toString()));
		assert length >= 0 : String.format("invalid array length `%d`", length);
		this.length = length;
		this.elementType = elementType;
	}

	@Override
	public boolean convertibleTo(Type otherType) {
		if (super.convertibleTo(otherType)) return true;
		if (otherType instanceof Pointer pointer) return elementType.equals(pointer.elementType);
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Array array)) return false;
		return length == array.length && Objects.equals(elementType, array.elementType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(elementType, length);
	}
}