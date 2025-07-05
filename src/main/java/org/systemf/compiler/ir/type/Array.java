package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.type.interfaces.DummyIndexableType;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.interfaces.Type;

import java.util.Objects;

public class Array extends DummyIndexableType implements Sized {
	final public int length;

	public Array(int length, Sized elementType) {
		super(String.format("[%d x %s]", length, elementType.getName()), elementType);
		assert length >= 0 : String.format("invalid array length `%d`", length);
		this.length = length;
	}

	@Override
	public boolean convertibleTo(Type otherType) {
		if (super.convertibleTo(otherType)) return true;
		if (otherType instanceof Pointer pointer) return elementType.equals(pointer.getElementType());
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Array array)) return false;
		if (!super.equals(o)) return false;
		return length == array.length;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), length);
	}
}