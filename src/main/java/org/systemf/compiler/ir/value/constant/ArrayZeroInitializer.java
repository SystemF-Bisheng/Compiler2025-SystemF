package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.type.interfaces.Sized;

public class ArrayZeroInitializer extends DummyArray {
	private final Constant content;

	public ArrayZeroInitializer(Sized elementType, int size) {
		super(elementType, size);
		this.content = genContent();
	}

	private Constant genContent() {
		if (elementType instanceof Array arr) return new ArrayZeroInitializer(arr.getElementType(), arr.length);
		else if (elementType == I32.INSTANCE) return ConstantInt.valueOf(0);
		else if (elementType == Float.INSTANCE) return ConstantFloat.valueOf(0);
		else throw new IllegalArgumentException("Unsupported element type " + elementType);
	}

	@Override
	public Constant getContent(int index) {
		if (index >= size) throw new IndexOutOfBoundsException();
		return content;
	}

	@Override
	public String toString() {
		return String.format("(zero-initialized %s)", type);
	}
}