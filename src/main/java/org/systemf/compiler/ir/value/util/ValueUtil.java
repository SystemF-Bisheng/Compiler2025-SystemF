package org.systemf.compiler.ir.value.util;

import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.global.IGlobal;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.constant.ConstantFloat;
import org.systemf.compiler.ir.value.constant.ConstantInt;

public class ValueUtil {
	static public String dumpIdentifier(Value value) {
		if (value instanceof IGlobal global) return "@" + global.getName();
		if (value instanceof INamed named) return "%" + named.getName();
		return value.toString();
	}

	static public String getName(Value value) {
		if (value instanceof INamed named) return named.getName();
		throw new IllegalArgumentException("Value " + value + " is not a named");
	}

	static public long getConstantInt(Value value) {
		if (!(value instanceof ConstantInt constantInt))
			throw new IllegalArgumentException("Value " + value + " is not a constant int");

		return constantInt.value;
	}

	static public double getConstantFloat(Value value) {
		if (!(value instanceof ConstantFloat constantFloat))
			throw new IllegalArgumentException("Value " + value + " is not a constant float");

		return constantFloat.value;
	}

	public static Constant assertConstant(Value value) {
		if (value instanceof Constant c) return c;
		throw new IllegalArgumentException("Value `" + value + "` is not a constant");
	}
}