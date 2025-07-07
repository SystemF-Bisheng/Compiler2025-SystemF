package org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous;

import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

public class Unreachable extends DummyNonTerminal {
	@Override
	public String toString() {
		return "unreachable";
	}
}