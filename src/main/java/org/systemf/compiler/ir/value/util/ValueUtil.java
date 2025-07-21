package org.systemf.compiler.ir.value.util;

import org.systemf.compiler.analysis.FunctionRepeatableResult;
import org.systemf.compiler.analysis.FunctionSideEffectResult;
import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.IGlobal;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.constant.ConstantFloat;
import org.systemf.compiler.ir.value.constant.ConstantInt;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.PotentialNonRepeatable;
import org.systemf.compiler.ir.value.instruction.PotentialSideEffect;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.query.QueryManager;

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

	public static boolean trivialInterchangeable(Value a, Value b) {
		if (a == b) return true;
		if (a instanceof Constant) return a.contentEqual(b);
		return false;
	}

	public static boolean sideEffect(Module module, Instruction inst) {
		if (inst instanceof AbstractCall call) {
			if (!(call.getFunction() instanceof Function func)) return true;
			var analysis = QueryManager.getInstance().getAttribute(module, FunctionSideEffectResult.class);
			return analysis.sideEffect(func);
		} else return inst instanceof PotentialSideEffect;
	}

	public static boolean sideEffect(Module module, Value value) {
		if (value instanceof Instruction inst) return sideEffect(module, inst);
		return false;
	}

	public static boolean repeatable(Module module, Instruction inst) {
		if (inst instanceof AbstractCall call) {
			if (!(call.getFunction() instanceof Function func)) return false;
			var analysis = QueryManager.getInstance().getAttribute(module, FunctionRepeatableResult.class);
			return analysis.repeatable(func);
		} else return !(inst instanceof PotentialNonRepeatable);
	}

	public static boolean repeatable(Module module, Value value) {
		if (value instanceof Instruction inst) return repeatable(module, inst);
		return true;
	}
}