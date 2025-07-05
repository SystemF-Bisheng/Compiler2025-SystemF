package org.systemf.compiler.ir.type.interfaces;

public abstract class DummyType implements Type {
	protected final String typeName;

	protected DummyType(String typeName) {
		this.typeName = typeName;
	}

	@Override
	public String getName() {
		return typeName;
	}

	@Override
	public boolean convertibleTo(Type otherType) {
		return this.equals(otherType);
	}

	@Override
	public String toString() {
		return typeName;
	}
}