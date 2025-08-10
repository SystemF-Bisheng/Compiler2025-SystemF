package org.systemf.compiler.ir.value.instruction.nonterminal.invoke;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.global.IFunction;
import org.systemf.compiler.ir.type.FunctionType;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.DummyInstruction;
import org.systemf.compiler.ir.value.instruction.PotentialBlockSensitive;
import org.systemf.compiler.ir.value.instruction.PotentialSideEffect;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.SequencedSet;

public abstract class AbstractCall extends DummyInstruction implements PotentialSideEffect, PotentialBlockSensitive {
	protected IFunction func;
	protected Value[] args;

	protected AbstractCall(IFunction func, Value... args) {
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

	public IFunction getFunction() {
		return func;
	}

	public void setFunction(IFunction func) {
		if (!(func.getType() instanceof FunctionType))
			throw new IllegalArgumentException("The type of the function must be a function type");
		if (this.func != null) this.func.unregisterDependant(this);
		this.func = func;
		func.registerDependant(this);
	}

	@Override
	public SequencedSet<ITracked> getDependency() {
		var usages = new LinkedHashSet<ITracked>(Arrays.asList(args));
		usages.add(func);
		return usages;
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
		if (func == oldValue) setFunction((IFunction) newValue);
		for (int i = 0; i < args.length; i++)
			if (args[i] == oldValue) {
				oldValue.unregisterDependant(this);
				args[i] = (Value) newValue;
				newValue.registerDependant(this);
			}
	}

	@Override
	public void unregister() {
		if (func != null) func.unregisterDependant(this);
		if (args != null) for (var arg : args) {
			if (arg == null) continue;
			arg.unregisterDependant(this);
		}
	}

	public Value[] getArgs() {
		return Arrays.copyOf(args, args.length);
	}

	public void setArgs(Value[] args) {
		if (this.args != null) for (var arg : this.args) {
			if (arg == null) continue;
			arg.unregisterDependant(this);
		}

		this.args = Arrays.copyOf(args, args.length);

		for (var arg : args) {
			if (arg == null) continue;
			arg.registerDependant(this);
		}
	}
}