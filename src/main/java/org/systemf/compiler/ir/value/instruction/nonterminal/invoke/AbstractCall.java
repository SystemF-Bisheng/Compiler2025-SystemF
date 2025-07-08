package org.systemf.compiler.ir.value.instruction.nonterminal.invoke;

import org.systemf.compiler.ir.type.FunctionType;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Arrays;

public abstract class AbstractCall extends DummyNonTerminal {
	private Value func;
	private Value[] args;

	protected AbstractCall(Value func, Value... args) {
		setFunction(func);
		setArgs(args);
	}

	protected String dumpCallBody() {
		StringBuilder result = new StringBuilder();
		result.append(ValueUtil.dumpIdentifier(func));
		result.append("(");
		for (int i = 0; i < args.length; i++) {
			if (i > 0) result.append(", ");
			result.append(ValueUtil.dumpIdentifier(args[i]));
		}
		result.append(")");
		return result.toString();
	}

	public Value getFunction() {
		return func;
	}

	public void setFunction(Value func) {
		if (!(func.getType() instanceof FunctionType))
			throw new IllegalArgumentException("The type of the function must be a function type");
		this.func = func;
	}

	public Value[] getArgs() {
		return Arrays.copyOf(args, args.length);
	}

	public void setArgs(Value[] args) {
		this.args = Arrays.copyOf(args, args.length);
	}
}