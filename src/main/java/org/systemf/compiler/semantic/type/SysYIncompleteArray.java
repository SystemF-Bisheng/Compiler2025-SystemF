package org.systemf.compiler.semantic.type;

public record SysYIncompleteArray(SysYType element) implements SysYType, ISysYArray {
	@Override
	public String toString() {
		return String.format("[%s]", element);
	}

	@Override
	public SysYType getElement() {
		return element;
	}
}