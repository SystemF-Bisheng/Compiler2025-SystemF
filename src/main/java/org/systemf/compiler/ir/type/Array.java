package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.type.interfaces.DummyIndexableType;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.interfaces.Type;

import java.util.Objects;

public class Array extends DummyIndexableType implements Sized {
	final public int length;
	private Array mergedArray = null;

	public Array(int length, Sized elementType) {
		super(String.format("[%d x %s]", length, elementType.getName()), elementType);
		if (length <= 0) throw new IllegalArgumentException("Illegal array length " + length);
		this.length = length;
	}

	@Override
	public boolean convertibleTo(Type otherType) {
		if (super.convertibleTo(otherType)) return true;
		if (otherType instanceof UnsizedArray arr) return elementType.equals(arr.getElementType());
		if (elementType instanceof Array innerArr) {
			if (mergedArray == null) mergedArray = new Array(this.length * innerArr.length, innerArr.elementType);
			return mergedArray.convertibleTo(otherType);
		}
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