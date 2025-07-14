package org.systemf.compiler.interpreter.value;


public class ArrayValue implements ExecutionValue {
	private final ExecutionValue[] values;

	public ArrayValue(ExecutionValue[] values) {
		this.values = values;
	}

	public ExecutionValue getValue(int index) {
		return values[index];
	}

	public ExecutionValue[] getValues() {
		return values;
	}

	public void setValue(int index, ExecutionValue value) {
		values[index] = value;
	}

	public int getLength() {
		return values.length;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < values.length; i++) {
			sb.append(values[i].toString());
			if (i < values.length - 1) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

}
