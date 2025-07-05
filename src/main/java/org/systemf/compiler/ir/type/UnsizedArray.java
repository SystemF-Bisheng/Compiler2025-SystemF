package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.type.interfaces.DummyIndexableType;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.interfaces.Type;

public class UnsizedArray extends DummyIndexableType {
	public UnsizedArray(Sized elementType) {
		super(String.format("[? x %s]", elementType.getName()), elementType);
	}

	@Override
	public boolean convertibleTo(Type otherType) {
		if (super.convertibleTo(otherType)) return true;
		if (otherType instanceof Pointer pointer) return elementType.equals(pointer.getElementType());
		return false;
	}
}