package org.systemf.compiler.ir.value.instruction.nonterminal.invoke;

import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

import java.util.Arrays;

public abstract class AbstractCall extends DummyNonTerminal {
	public final Value func;
	public final Value[] args;

	protected AbstractCall(Value func, Value... args) {
		this.func = func;
		this.args = Arrays.copyOf(args, args.length);
	}
}