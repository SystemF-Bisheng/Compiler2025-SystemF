package org.systemf.compiler.semantic.type;

public record SysYArray(SysYType element) implements SysYType {
	@Override
	public boolean convertibleTo(SysYType other) {
		if (equals(other)) return true;
		return other instanceof SysYIncompleteArray(SysYType otherElement) && element.equals(otherElement);
	}

	@Override
	public String toString() {
		return element + "[?]";
	}
}