package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;

public class ConstantInt extends DummyConstant {
	private static final ConstantInt[] cache = new ConstantInt[1025];
	final public long value;

	private ConstantInt(long value) {
		super(I32.INSTANCE);
		this.value = value;
	}

	public static ConstantInt valueOf(long value) {
		if (-512 <= value && value <= 512) {
			var cacheIndex = (int) (value + 512);
			if (cache[cacheIndex] == null) cache[cacheIndex] = new ConstantInt(value);
			return cache[cacheIndex];
		} else return new ConstantInt(value);
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public boolean contentEqual(Value other) {
		if (!super.contentEqual(other)) return false;
		var otherInt = (ConstantInt) other;
		return value == otherInt.value;
	}
}