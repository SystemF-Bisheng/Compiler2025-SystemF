package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.Type;
import org.systemf.compiler.ir.value.instruction.nonterminal.NonTerminal;

public class Alloca extends NonTerminal {
	public Alloca(String name, Type type) {
		super(new Array(type), name);
	}
}