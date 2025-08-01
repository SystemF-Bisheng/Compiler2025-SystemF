package org.systemf.compiler.lower.rv64gc.module.position;

public record RVStackPos(long offset) implements RVPosition {
	@Override
	public String toString() {
		return "stack " + offset;
	}
}
