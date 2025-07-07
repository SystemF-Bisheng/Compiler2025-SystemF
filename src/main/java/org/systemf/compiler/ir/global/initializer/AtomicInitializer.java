package org.systemf.compiler.ir.global.initializer;

import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.ConstantFloat;
import org.systemf.compiler.ir.value.constant.ConstantInt;

public record AtomicInitializer(Value value) implements IGlobalInitializer {
	@Override
	public String toString() {
		if (value instanceof ConstantInt constantInt) {
			return Long.toString(constantInt.value);
		} else if (value instanceof ConstantFloat constantFloat) {
			return Double.toString(constantFloat.value);
		} else {
			throw new IllegalArgumentException("Unsupported value type in atomic initializer");
		}

	}
}