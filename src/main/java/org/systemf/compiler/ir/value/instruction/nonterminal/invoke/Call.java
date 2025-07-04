package org.systemf.compiler.ir.value.instruction.nonterminal.invoke;

import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.util.ReturnTypeGetter;

public class Call extends DummyNonTerminal {
	public final Value func;
	public final Value[] args;

	public Call(String name, Value func, Value... args) {
		super(ReturnTypeGetter.get(func.getType()), name);
		this.func = func;
		this.args = args;
	}
}