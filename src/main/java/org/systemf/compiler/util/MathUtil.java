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
}
