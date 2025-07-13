package org.systemf.compiler.interpreter.value;

public record IntValue(int value) implements ExecutionValue {


	@Override
	public String toString() {
		return String.valueOf(value);
	}

}

