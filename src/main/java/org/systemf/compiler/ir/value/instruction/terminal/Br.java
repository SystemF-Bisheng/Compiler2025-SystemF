package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.block.BasicBlock;

public class Br extends DummyTerminal {
	private BasicBlock target;

	public Br(BasicBlock target) {
		setTarget(target);
	}

	@Override
	public String toString() {
		return String.format("br %s", target.getName());
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	public BasicBlock getTarget() {
		return target;
	}

	public void setTarget(BasicBlock target) {
		this.target = target;
	}
}