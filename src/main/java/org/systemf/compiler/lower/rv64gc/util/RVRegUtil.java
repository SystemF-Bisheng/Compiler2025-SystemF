package org.systemf.compiler.lower.rv64gc.util;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.type.I64;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.lower.rv64gc.module.position.RVRegister;
import org.systemf.compiler.lower.rv64gc.module.register.RVRegisterType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RVRegUtil {
	public static final EnumMap<RVRegisterType, Integer> AVAILABLE_CNT = new EnumMap<>(
			Map.of(RVRegisterType.INTEGER, 22, RVRegisterType.FLOAT, 29));
	public static final EnumMap<RVRegisterType, Set<Integer>> AVAILABLE_SAVED = new EnumMap<>(
			Map.of(RVRegisterType.INTEGER, Set.of(9, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27), RVRegisterType.FLOAT,
					Set.of(8, 9, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
	public static final EnumMap<RVRegisterType, Set<Integer>> AVAILABLE_NON_SAVED = new EnumMap<>(
			Map.of(RVRegisterType.INTEGER, Set.of(10, 11, 12, 13, 14, 15, 16, 17, 29, 30, 31), RVRegisterType.FLOAT,
					Set.of(3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 15, 16, 17, 28, 29, 30, 31)));
	public static final EnumMap<RVRegisterType, Set<Integer>> AVAILABLE_TEMPORARY = new EnumMap<>(
			Map.of(RVRegisterType.INTEGER, Set.of(6, 7, 28), RVRegisterType.FLOAT, Set.of(0, 1, 2)));
	public static final EnumMap<RVRegisterType, Integer> RETURN_REGISTER = new EnumMap<>(
			Map.of(RVRegisterType.INTEGER, 10, RVRegisterType.FLOAT, 10));
	public static final RVRegister INTEGER_SCRATCH_REGISTER = new RVRegister(RVRegisterType.INTEGER, 5);
	public static final RVRegister FRAME_POINTER = new RVRegister(RVRegisterType.INTEGER, 8);
	public static final RVRegister STACK_POINTER = new RVRegister(RVRegisterType.INTEGER, 2);
	public static final RVRegister RETURN_ADDRESS = new RVRegister(RVRegisterType.INTEGER, 1);
	public static final int IMM_WIDTH = 12;
	public static final int REG_SIZE = 8;
	public static final int DEFAULT_STACK_ALIGNMENT = 16;
	public static final EnumMap<RVRegisterType, List<Integer>> PARAMETER_REGISTERS = new EnumMap<>(
			Map.of(RVRegisterType.INTEGER, List.of(10, 11, 12, 13, 14, 15, 16, 17), RVRegisterType.FLOAT,
					List.of(10, 11, 12, 13, 14, 15, 16, 17)));

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

	public static boolean isSaved(RVRegister register) {
		return AVAILABLE_SAVED.get(register.type()).contains(register.index());
	}

	public static boolean isNonSaved(RVRegister register) {
		return AVAILABLE_NON_SAVED.get(register.type()).contains(register.index());
	}
}
