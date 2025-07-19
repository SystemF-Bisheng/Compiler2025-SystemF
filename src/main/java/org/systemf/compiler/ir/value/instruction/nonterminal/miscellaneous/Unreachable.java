package org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

import java.util.Collections;
import java.util.Set;

public class Unreachable extends DummyNonTerminal {
	public static final Unreachable INSTANCE = new Unreachable();

	private Unreachable() {}

	@Override
	public String toString() {
		return "unreachable";
	}

	@Override
	public Set<ITracked> getDependency() {
		return Collections.emptySet();
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void unregister() {
	}
}