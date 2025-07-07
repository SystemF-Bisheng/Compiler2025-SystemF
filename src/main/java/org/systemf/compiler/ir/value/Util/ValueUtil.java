package org.systemf.compiler.ir.value.Util;

import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.ConstantFloat;
import org.systemf.compiler.ir.value.constant.ConstantInt;

public class ValueUtil {

	static public String getValueName(Value value) {
		if (value instanceof ConstantInt constantInt) {
			return Long.toString(ValueUtil.getConstantValueInt(constantInt));
		}
		if (value instanceof ConstantFloat constantFloat) {
			return Double.toString(ValueUtil.getConstantValueFloat(constantFloat));
		}
		if (!(value instanceof INamed named)) {
			throw new IllegalArgumentException("Value " + value + " is not a valueInstruction");
		}
		return named.getName();
	}

	static public long getConstantValueInt(Value value) {
		if (!(value instanceof ConstantInt constantInt)) {
			throw new IllegalArgumentException("Value " + value + " is not a constantInt");
		}

		return constantInt.value;
	}

	static public double getConstantValueFloat(Value value) {
		if (!(value instanceof ConstantFloat constantFloat)) {
			throw new IllegalArgumentException("Value " + value + " is not a constantFloat");
		}

		return constantFloat.value;
	}

}