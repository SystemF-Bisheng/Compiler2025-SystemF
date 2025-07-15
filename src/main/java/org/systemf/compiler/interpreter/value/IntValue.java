package org.systemf.compiler.interpreter.value;

public class IntValue implements ExecutionValue {
	private int value;

	public IntValue(int value) {
		this.value = value;
	}


	public void setValue(ExecutionValue value) {
		if (!(value instanceof IntValue)) {
			throw new IllegalArgumentException("Expected IntValue, but got " + value.getClass().getSimpleName());
		}
		this.value = ((IntValue) value).value;
	}

	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

}
