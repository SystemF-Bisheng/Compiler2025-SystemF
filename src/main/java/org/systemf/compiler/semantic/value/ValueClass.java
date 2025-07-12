package org.systemf.compiler.semantic.value;

public enum ValueClass {
	LEFT, RIGHT;

	public boolean convertibleTo(ValueClass other) {
		if (this == other) return true;
		return this == LEFT && other == RIGHT;
	}
}