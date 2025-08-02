package org.systemf.compiler.util;

public class MathUtil {
	public static int checkPowerOfTwo(long val) {
		int res = 0;
		while (val > 0) {
			if ((val & 1) == 1) break;
			++res;
			val >>= 1;
		}
		if (val == 1) return res;
		return -1;
	}

	public static int roundTo(int val, int d) {
		var mod = val % d;
		if (mod == 0) return val;
		return val + (d - mod);
	}

	public static long roundTo(long val, long d) {
		var mod = val % d;
		if (mod == 0) return val;
		return val + (d - mod);
	}
}
