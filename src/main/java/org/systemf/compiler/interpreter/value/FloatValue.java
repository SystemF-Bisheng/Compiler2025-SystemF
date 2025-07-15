package org.systemf.compiler.interpreter.value;

public class FloatValue implements ExecutionValue {
	private float value;

	public FloatValue(float value) {
		this.value = value;
	}


	public void setValue(ExecutionValue value) {
		if (!(value instanceof FloatValue)) {
			throw new IllegalArgumentException("Expected FloatValue, but got " + value.getClass().getSimpleName());
		}
		this.value = ((FloatValue) value).value;
	}

	public float getValue() {
		return value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

}
