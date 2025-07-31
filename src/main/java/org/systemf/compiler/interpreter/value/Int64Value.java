package org.systemf.compiler.interpreter.value;

public class Int64Value implements ExecutionValue {
	private static final Int64Value[] cache = new Int64Value[2049];
	private final long value;

	public Int64Value(long value) {
		this.value = value;
	}

	@Override
	public ExecutionValue clone() {
		return new Int64Value(this.value);
	}

	public long getValue() {
		return value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	public static ExecutionValue valueOf(long value) {
		if (-1024 <= value && value <= 1024) {
			int cacheIndex = (int)(value + 1024);
			if (cache[cacheIndex] == null) cache[cacheIndex] = new Int64Value(value);
			return cache[cacheIndex];
		} else return new Int64Value(value);
	}

}