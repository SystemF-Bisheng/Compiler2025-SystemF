package org.systemf.compiler.semantic.type;

public record SysYIncompleteArray(SysYType element) implements SysYType {
	@Override
	public String toString() {
		return element + "[]";
	}
}