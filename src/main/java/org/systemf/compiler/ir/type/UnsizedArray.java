package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.type.interfaces.DummyIndexableType;
import org.systemf.compiler.ir.type.interfaces.Sized;

public class UnsizedArray extends DummyIndexableType {
	public UnsizedArray(Sized elementType) {
		super(String.format("[? x %s]", elementType.getName()), elementType);
	}
}