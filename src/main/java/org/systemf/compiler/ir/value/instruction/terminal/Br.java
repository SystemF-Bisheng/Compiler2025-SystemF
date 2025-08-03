package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.util.CollectionUtil;

import java.util.SequencedSet;

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
	public SequencedSet<ITracked> getDependency() {
		return CollectionUtil.singletonSequencedSet(target);
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
		if (target == oldValue) setTarget((BasicBlock) newValue);
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void unregister() {
		if (target != null) target.unregisterDependant(this);
	}

	public BasicBlock getTarget() {
		return target;
	}

	public void setTarget(BasicBlock target) {
		if (this.target != null) this.target.unregisterDependant(this);
		this.target = target;
		target.registerDependant(this);
	}
}