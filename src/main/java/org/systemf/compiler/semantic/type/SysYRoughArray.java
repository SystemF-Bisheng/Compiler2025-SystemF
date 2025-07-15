package org.systemf.compiler.semantic.type;

public record SysYRoughArray(SysYType element) implements SysYType, ISysYArray {
	@Override
	public boolean convertibleTo(SysYType other) {
		if (equals(other)) return true;
		return other instanceof SysYIncompleteArray(SysYType otherElement) && element.equals(otherElement);
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