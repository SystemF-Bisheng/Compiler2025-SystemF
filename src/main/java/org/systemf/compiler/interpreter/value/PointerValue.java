package org.systemf.compiler.interpreter.value;

import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.interfaces.Type;

import java.util.function.Supplier;

public class PointerValue implements ExecutionValue {
	private final int startIndex;
	private final int endIndex;
	public ExecutionValue value;
	public Type type;
	private PointerValue[] cache;

	public PointerValue(ArrayValue arrayValue, Type type) {
		this.value = arrayValue;
		this.type = type;
		this.startIndex = 0;
		this.endIndex = arrayValue.getLength() - 1;
		this.cache = new PointerValue[arrayValue.getLength()];
	}

	public PointerValue(ArrayValue arrayValue, Type type, int startIndex) {
		this.value = arrayValue;
		this.type = type;
		this.startIndex = startIndex;
		if (type instanceof Array array) {
			int length = getArrayLength(array);
			this.endIndex = startIndex + length - 1;
			this.cache = new PointerValue[array.length];
		} else endIndex = startIndex;
	}


	public void setValue(ExecutionValue newValue) {
		ExecutionValue[] values = ((ArrayValue) value).values();
		if (newValue instanceof IntValue || newValue instanceof FloatValue) {
			values[startIndex] = newValue;
			return;
		}
		PointerValue v = (PointerValue) newValue;
		ExecutionValue[] newValues = v.getValues();
		int t = v.startIndex;
		for (int i = startIndex; i <= endIndex; i++) {
			values[i] = newValues[t];
			t++;
		}
	}

	public void setValue(int i, ExecutionValue newValue) {
		ExecutionValue[] values = ((ArrayValue) value).values();
		values[startIndex + i] = newValue;
	}

	@Override
	public ExecutionValue clone() {
		ExecutionValue[] clonedValues = new ExecutionValue[((ArrayValue) value).getLength()];
		for (int i = 0; i < clonedValues.length; i++) {
			clonedValues[i] = ((ArrayValue) value).getValue(startIndex + i).clone();
		}
		return new PointerValue(new ArrayValue(clonedValues), type, startIndex);
	}

	private PointerValue getOrProduceCache(int index, Supplier<PointerValue> supplier) {
		if (0 <= index && index < cache.length) {
			if (cache[index] != null) return cache[index];
			var value = supplier.get();
			cache[index] = value;
			return value;
		} else return supplier.get();
	}

	public ExecutionValue getValue(int index) {
		Array arrayType = (Array) type;
		if (arrayType.getElementType() instanceof Array array) return getOrProduceCache(index,
				() -> new PointerValue((ArrayValue) value, array, startIndex + index * getArrayLength(array)));
		else return getOrProduceCache(index,
				() -> new PointerValue((ArrayValue) value, arrayType.getElementType(), startIndex + index));
	}

	private int getArrayLength(Array array) {
		int length = array.length;
		while (array.getElementType() instanceof Array innerArray) {
			length *= innerArray.length;
			array = innerArray;
		}
		return length;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public ExecutionValue[] getValues() {
		return ((ArrayValue) value).values();
	}

	public ExecutionValue getStart() {
		return ((ArrayValue) value).getValue(startIndex);
	}
}
