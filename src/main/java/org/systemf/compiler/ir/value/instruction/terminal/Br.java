package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.block.BasicBlock;

public class Br extends DummyTerminal {
	public final BasicBlock target;

	public Br(BasicBlock target) {
		this.target = target;
	}

	@Override
	public String toString() {
		return String.format("br %s", target.getName());
	}
}