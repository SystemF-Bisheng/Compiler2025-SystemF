package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

public class Store extends DummyNonTerminal {
	public final Value src, dest;

	public Store(Value src, Value dest) {
		this.src = src;
		this.dest = dest;
	}
}