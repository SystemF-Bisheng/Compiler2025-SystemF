package org.systemf.compiler.interpreter.value;

public class IntValue implements ExecutionValue {
	private static final IntValue[] cache = new IntValue[2049];
	private int value;

	public IntValue(int value) {
		this.value = value;
	}


	@Override
	public ExecutionValue clone() {
		return new IntValue(this.value);
	}

	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	public static ExecutionValue valueOf(int value) {
		if (-1024 <= value && value <= 1024) {
			int cacheIndex = value + 1024;
			if (cache[cacheIndex] == null) cache[cacheIndex] = new IntValue(value);
			return cache[cacheIndex];
		} else return new IntValue(value);
	}

}