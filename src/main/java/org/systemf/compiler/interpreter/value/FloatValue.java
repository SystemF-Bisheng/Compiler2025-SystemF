package org.systemf.compiler.interpreter.value;

public record FloatValue(float value) implements ExecutionValue {


	@Override
	public String toString() {
		return String.valueOf(value);
	}

}
