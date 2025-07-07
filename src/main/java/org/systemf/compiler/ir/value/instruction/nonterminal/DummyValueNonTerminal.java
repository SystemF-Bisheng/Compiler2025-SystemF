package org.systemf.compiler.ir.value.instruction.nonterminal;

import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.instruction.DummyValueInstruction;

public abstract class DummyValueNonTerminal extends DummyValueInstruction {
	public DummyValueNonTerminal(Type type, String name) {
		super(type, name);
	}
}