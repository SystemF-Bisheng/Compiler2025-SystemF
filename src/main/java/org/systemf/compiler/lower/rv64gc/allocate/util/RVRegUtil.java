package org.systemf.compiler.lower.rv64gc.allocate.util;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.type.I64;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;

public class RVRegUtil {
	public static final int REG_TYPE_CNT = 2;
	public static final int[] AVAILABLE_CNT = new int[]{23, 29};

	public static int regType(Value value) {
		return regType(value.getType());
	}

	public static int regType(Type type) {
		return switch (type) {
			case I32 _ -> 0;
			case I64 _ -> 0;
			case Float _ -> 1;
			case null, default -> throw new UnsupportedOperationException("Unsupported type: " + type);
		};
	}
}
