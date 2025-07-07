package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.type.interfaces.DummyDereferenceableType;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.interfaces.Type;

public class Pointer extends DummyDereferenceableType implements Sized {
	public Pointer(Type elementType) {
		super(String.format("%s*", elementType.getName()), elementType);
	}
}