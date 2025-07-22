package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;

import java.util.Collections;
import java.util.Set;

public class RetVoid extends DummyTerminal {
	public static final RetVoid INSTANCE = new RetVoid();

	private RetVoid() {}

	@Override
	public Set<ITracked> getDependency() {
		return Collections.emptySet();
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
	}

	@Override
	public String toString() {
		return "ret void";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void unregister() {
	}
}