package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.value.Value;

public abstract class DummyArray extends DummyConstant implements ConstantArray {
	protected final Sized elementType;
	protected final int size;

	protected DummyArray(Sized elementType, int size) {
		super(new Array(size, elementType));
		this.elementType = elementType;
		this.size = size;
	}

	@Override
	public Sized getElementType() {
		return elementType;
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public boolean contentEqual(Value other) {
		if (!super.contentEqual(other)) return false;
		var otherArray = (DummyArray) other;
		return elementType.equals(otherArray.elementType) && size == otherArray.size;
	}
}