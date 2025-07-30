package org.systemf.compiler.util;

import java.util.Optional;

public class SaturationArithmetic {
	public static Optional<Long> checkedAdd(long x, long y) {
		long r = x + y;
		if (((x ^ r) & (y ^ r)) < 0L) return Optional.empty();
		else return Optional.of(r);
	}

	public static Optional<Long> checkedMul(long x, long y) {
		long r = x * y;
		long ax = Math.abs(x);
		long ay = Math.abs(y);
		if ((ax | ay) >>> 31 == 0L || (y == 0L || r / y == x) && (x != Long.MIN_VALUE || y != -1L))
			return Optional.of(r);
		else return Optional.empty();
	}

	public static Optional<Long> checkedNeg(long a) {
		if (a == Long.MIN_VALUE) return Optional.empty();
		else return Optional.of(-a);
	}

	public static boolean isOverflow(long v, int width) {
		if (width == 32) {
			if (v > Integer.MAX_VALUE) return true;
			return v < Integer.MIN_VALUE;
		} else if (width == 64) return false;
		throw new IllegalArgumentException("Unsupported width: " + width);
	}

	public static int saturated(long v) {
		if (v > Integer.MAX_VALUE) return Integer.MAX_VALUE;
		if (v < Integer.MIN_VALUE) return Integer.MIN_VALUE;
		return (int) v;
	}

	public static int saturatedAdd(int x, int y) {
		return saturated((long) x + y);
	}

	public static int saturatedSub(int x, int y) {
		return saturated((long) x - y);
	}

	public static int saturatedMul(int x, int y) {
		return saturated((long) x * y);
	}

	public static int saturatedDiv(int x, int y) {
		return saturated((long) x / y);
	}

	public static int saturatedLerp(int V, int w, int W) {
		long tmp = V;
		tmp *= w;
		long rounding = tmp % W * 2 >= W ? 1 : 0;
		tmp /= W;
		tmp += rounding;
		return saturated(tmp);
	}
}
