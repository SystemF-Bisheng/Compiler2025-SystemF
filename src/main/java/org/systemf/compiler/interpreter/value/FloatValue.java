package org.systemf.compiler.interpreter.value;

public class FloatValue implements ExecutionValue {
	private static final FloatValue ZERO = new FloatValue(0f);
	private float value;

	public FloatValue(float value) {
		this.value = value;
	}


	public float getValue() {
		return value;
	}


	public ExecutionValue clone() {
		return new FloatValue(this.value);
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	public static ExecutionValue valueOf(float value) {
		if (value == 0) {
			return ZERO;
		} else return new FloatValue(value);
	}
}