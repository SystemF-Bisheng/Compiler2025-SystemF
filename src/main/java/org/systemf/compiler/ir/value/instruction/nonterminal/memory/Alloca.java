package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

public class Alloca extends DummyNonTerminal {
	public Alloca(String name, Type type) {
		super(new Pointer(type), name);
	}

	@Override
	public String toString() {
		return String.format("%%%s = alloca %s", name, type.getName());
	}
}