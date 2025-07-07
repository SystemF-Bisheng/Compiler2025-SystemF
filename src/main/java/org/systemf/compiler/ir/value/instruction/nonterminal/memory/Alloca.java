package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;

public class Alloca extends DummyValueNonTerminal {
	public Alloca(String name, Type type) {
		super(new Pointer(type), name);
	}
}