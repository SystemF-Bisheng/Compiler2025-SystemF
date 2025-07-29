package org.systemf.compiler.util;

public class SaturationArithmetic {
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
