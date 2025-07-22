package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.type.interfaces.Atom;
import org.systemf.compiler.ir.type.interfaces.DummyDereferenceableType;
import org.systemf.compiler.ir.type.interfaces.Type;

public class Pointer extends DummyDereferenceableType implements Atom {
	public Pointer(Type elementType) {
		super(String.format("%s*", elementType.getName()), elementType);
	}

	@Override
	public boolean convertibleTo(Type otherType) {
		if (super.convertibleTo(otherType)) return true;
		return otherType instanceof Pointer ptr && elementType.convertibleTo(ptr.getElementType());
	}
}