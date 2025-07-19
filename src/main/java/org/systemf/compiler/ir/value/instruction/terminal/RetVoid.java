package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.value.Value;

import java.util.Collections;
import java.util.Set;

public class RetVoid extends DummyTerminal {
	public RetVoid() {
	}

	@Override
	public Set<Value> getDependency() {
		return Collections.emptySet();
	}

	@Override
	public void replaceAll(Value oldValue, Value newValue) {
	}

	@Override
	public void replaceAll(BasicBlock oldBlock, BasicBlock newBlock) {}

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