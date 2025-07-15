package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.type.interfaces.DummyIndexableType;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.interfaces.Type;

public class UnsizedArray extends DummyIndexableType {
	private UnsizedArray mergedArray = null;

	public UnsizedArray(Sized elementType) {
		super(String.format("[? x %s]", elementType.getName()), elementType);
	}

	@Override
	public boolean convertibleTo(Type otherType) {
		if (super.convertibleTo(otherType)) return true;
		if (elementType instanceof Array innerArr) {
			if (mergedArray == null) mergedArray = new UnsizedArray(innerArr.getElementType());
			return mergedArray.convertibleTo(otherType);
		}
		return false;
	}
}