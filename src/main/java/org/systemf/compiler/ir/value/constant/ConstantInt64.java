package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.I64;
import org.systemf.compiler.ir.value.Value;

public class ConstantInt64 extends DummyConstant implements ConstantInt {
	private static final ConstantInt64[] cache = new ConstantInt64[1025];
	final public long value;

	private ConstantInt64(long value) {
		super(I64.INSTANCE);
		this.value = value;
	}

	public static ConstantInt64 valueOf(long value) {
		if (-512 <= value && value <= 512) {
			var cacheIndex = (int) (value + 512);
			if (cache[cacheIndex] == null) cache[cacheIndex] = new ConstantInt64(value);
			return cache[cacheIndex];
		} else return new ConstantInt64(value);
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public boolean contentEqual(Value other) {
		if (!super.contentEqual(other)) return false;
		var otherInt = (ConstantInt64) other;
		return value == otherInt.value;
	}
}