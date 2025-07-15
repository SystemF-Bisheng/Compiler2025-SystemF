package org.systemf.compiler.semantic.type;

public record SysYIncompleteArray(SysYType element) implements SysYType, ISysYArray {
	@Override
	public boolean convertibleTo(SysYType other) {
		if (equals(other)) return true;
		if (element instanceof SysYRoughArray innerArr)
			return new SysYIncompleteArray(innerArr.getElement()).convertibleTo(other);
		return false;
	}

	@Override
	public String toString() {
		return String.format("[%s]", element);
	}

	@Override
	public SysYType getElement() {
		return element;
	}
}