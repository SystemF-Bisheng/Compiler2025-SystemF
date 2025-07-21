package org.systemf.compiler.interpreter.value;


public record ArrayValue(ExecutionValue[] values) implements ExecutionValue {
	public ExecutionValue getValue(int index) {
		return values[index];
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

	@Override
	public void setValue(ExecutionValue value) {
		throw new UnsupportedOperationException("ArrayValue does not support setValue without index");
	}

	@Override
	public ExecutionValue clone() {
		ExecutionValue[] clonedValues = new ExecutionValue[values.length];
		for (int i = 0; i < values.length; i++) {
			clonedValues[i] = values[i].clone();
		}
		return new ArrayValue(clonedValues);
	}
}