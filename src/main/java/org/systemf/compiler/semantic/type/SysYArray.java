package org.systemf.compiler.semantic.type;

public record SysYArray(SysYType element, long length) implements SysYType, ISysYArray {
	@Override
	public boolean convertibleTo(SysYType other) {
		if (equals(other)) return true;
		if (other instanceof SysYIncompleteArray(SysYType otherElement) && element.equals(otherElement)) return true;
		return other instanceof SysYRoughArray(SysYType otherElement) && element.equals(otherElement);
	}

	@Override
	public String toString() {
		return String.format("%s[%d]", element, length);
	}

	@Override
	public SysYType getElement() {
		return element;
	}
}