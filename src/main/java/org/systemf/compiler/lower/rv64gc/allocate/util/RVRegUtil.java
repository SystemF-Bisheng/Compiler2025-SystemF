package org.systemf.compiler.lower.rv64gc.allocate.util;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.type.I64;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.lower.rv64gc.module.register.RVRegisterType;

import java.util.EnumMap;
import java.util.Map;

public class RVRegUtil {
	public static final EnumMap<RVRegisterType, Integer> AVAILABLE_CNT = new EnumMap<>(
			Map.of(RVRegisterType.INTEGER, 23, RVRegisterType.FLOAT, 29));
	public static final EnumMap<RVRegisterType, int[]> AVAILABLE_SAVED = new EnumMap<>(
			Map.of(RVRegisterType.INTEGER, new int[]{9, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27}, RVRegisterType.FLOAT,
					new int[]{8, 9, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27}));
	public static final EnumMap<RVRegisterType, int[]> AVAILABLE_NON_SAVED = new EnumMap<>(
			Map.of(RVRegisterType.INTEGER, new int[]{10, 11, 12, 13, 14, 15, 16, 17, 28, 29, 30, 31},
					RVRegisterType.FLOAT, new int[]{3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 15, 16, 17, 28, 29, 30, 31}));
	public static final EnumMap<RVRegisterType, int[]> AVAILABLE_TEMPORARY = new EnumMap<>(
			Map.of(RVRegisterType.INTEGER, new int[]{5, 6, 7}, RVRegisterType.FLOAT, new int[]{0, 1, 2}));

	public static RVRegisterType regType(Value value) {
		return regType(value.getType());
	}

	public static RVRegisterType regType(Type type) {
		return switch (type) {
			case I32 _, I64 _ -> RVRegisterType.INTEGER;
			case Float _ -> RVRegisterType.FLOAT;
			case null, default -> throw new UnsupportedOperationException("Unsupported type: " + type);
		};
	}
}
