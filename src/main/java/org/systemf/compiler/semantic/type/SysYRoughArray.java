package org.systemf.compiler.semantic.type;

public record SysYRoughArray(SysYType element) implements SysYType, ISysYArray {
	@Override
	public boolean convertibleTo(SysYType other) {
		if (equals(other)) return true;
		if (other instanceof SysYIncompleteArray(SysYType otherElement) && element.equals(otherElement)) return true;
		if (element instanceof SysYRoughArray innerArr)
			return new SysYRoughArray(innerArr.getElement()).convertibleTo(other);
		return false;
	}

	@Override
	public String toString() {
		return String.format("[? x %s]", element);
	}

	@Override
	public SysYType getElement() {
		return element;
	}
}