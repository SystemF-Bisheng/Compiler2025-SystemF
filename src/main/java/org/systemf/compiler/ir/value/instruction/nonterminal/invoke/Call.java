package org.systemf.compiler.ir.value.instruction.nonterminal.invoke;

import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

public class Call extends DummyNonTerminal {
	public final Value func;
	public final Value[] args;

	public Call(String name, Value func, Value... args) {
		super(TypeUtil.getReturnType(func.getType()), name);
		this.func = func;
		this.args = args;
	}
}