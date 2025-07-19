package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.value.Value;

import java.util.Collections;
import java.util.Set;

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
	public Set<Value> getDependency() {
		return Collections.emptySet();
	}

	@Override
	public void replaceAll(Value oldValue, Value newValue) {
	}

	@Override
	public void replaceAll(BasicBlock oldBlock, BasicBlock newBlock) {
		if (target == oldBlock) setTarget(newBlock);
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void unregister() {
	}

	public BasicBlock getTarget() {
		return target;
	}

	public void setTarget(BasicBlock target) {
		this.target = target;
	}
}