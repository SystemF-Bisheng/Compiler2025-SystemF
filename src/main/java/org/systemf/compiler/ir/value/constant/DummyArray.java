package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.interfaces.Sized;

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
}