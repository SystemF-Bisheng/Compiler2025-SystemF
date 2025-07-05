package org.systemf.compiler.ir.type;

public class DummyIndexableType extends DummyType implements Indexable {
	protected final Type elementType;

	protected DummyIndexableType(String typeName, Type elementType) {
		super(typeName);
		this.elementType = elementType;
	}

	@Override
	public Type getElementType() {
		return elementType;
	}
}