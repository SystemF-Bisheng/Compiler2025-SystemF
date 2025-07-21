package org.systemf.compiler.interpreter.value;

public class FloatValue implements ExecutionValue {
	private float value;

	public FloatValue(float value) {
		this.value = value;
	}



	public float getValue() {
		return value;
	}

	@Override
	public void setValue(ExecutionValue value) {
		this.value = ((FloatValue) value).value;
	}

	public ExecutionValue clone() {
		return new FloatValue(this.value);
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

}