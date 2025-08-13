package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.ArrayZeroInitializer;
import org.systemf.compiler.ir.value.constant.ConstantArray;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Map;
import java.util.stream.IntStream;

public record IntegerSignAnalysisResult(Map<Value, IntegerSign> sign, Map<Value, IntegerSign> allocSign,
                                        Map<Function, IntegerSign> funcSign) {
	public IntegerSign sign(Value value) {
		if (ValueUtil.isConstantInt(value)) {
			var val = ValueUtil.getConstantInt(value);
			return IntegerSign.signOf(val);
		}
		if (value instanceof ConstantArray arr) {
			if (arr instanceof ArrayZeroInitializer) return IntegerSign.ZERO;
			return IntStream.range(0, arr.getSize()).mapToObj(arr::getContent).map(this::sign)
					.reduce(IntegerSign.UNDEFINED, IntegerSign::meet);
		}
		return sign.getOrDefault(value, IntegerSign.UNDEFINED);
	}

	public IntegerSign allocSign(Value value) {
		return allocSign.getOrDefault(value, IntegerSign.UNDEFINED);
	}

	public IntegerSign funcSign(Function function) {
		return funcSign.getOrDefault(function, IntegerSign.UNDEFINED);
	}

	public enum IntegerSign {
		UNDEFINED, NEGATIVE, POSITIVE, ZERO, NON_POSITIVE, NON_NEGATIVE, ALL;

		public static IntegerSign signOf(long value) {
			if (value == 0) return ZERO;
			if (value < 0) return NEGATIVE;
			return POSITIVE;
		}

		public IntegerSign neg() {
			return switch (this) {
				case UNDEFINED, ALL, ZERO -> this;
				case NEGATIVE -> POSITIVE;
				case POSITIVE -> NEGATIVE;
				case NON_POSITIVE -> NON_NEGATIVE;
				case NON_NEGATIVE -> NON_POSITIVE;
			};
		}

		public IntegerSign inv() {
			return switch (this) {
				case UNDEFINED, ZERO -> UNDEFINED;
				case NEGATIVE, NON_POSITIVE -> NEGATIVE;
				case POSITIVE, NON_NEGATIVE -> POSITIVE;
				case ALL -> ALL;
			};
		}

		public boolean subsetEq(IntegerSign other) {
			if (this == other) return true;
			return switch (this) {
				case UNDEFINED -> true;
				case NEGATIVE -> other == NON_POSITIVE || other == ALL;
				case POSITIVE -> other == NON_NEGATIVE || other == ALL;
				case ZERO -> other == NON_POSITIVE || other == NON_NEGATIVE || other == ALL;
				case NON_POSITIVE, NON_NEGATIVE -> other == ALL;
				case ALL -> false;
			};
		}

		public IntegerSign meet(IntegerSign other) {
			if (this.ordinal() > other.ordinal()) return other.meet(this);
			return switch (this) {
				case UNDEFINED, ZERO, NON_NEGATIVE -> other;
				case NEGATIVE -> switch (other) {
					case NEGATIVE -> NEGATIVE;
					case POSITIVE, NON_NEGATIVE, ALL -> ALL;
					case ZERO, NON_POSITIVE -> NON_POSITIVE;
					default -> throw new AssertionError();
				};
				case POSITIVE -> switch (other) {
					case POSITIVE -> POSITIVE;
					case ZERO, NON_NEGATIVE -> NON_NEGATIVE;
					case NON_POSITIVE, ALL -> ALL;
					default -> throw new AssertionError();
				};
				case NON_POSITIVE -> switch (other) {
					case NON_POSITIVE -> NON_POSITIVE;
					case NON_NEGATIVE, ALL -> ALL;
					default -> throw new AssertionError();
				};
				case ALL -> ALL;
			};
		}
	}
}
