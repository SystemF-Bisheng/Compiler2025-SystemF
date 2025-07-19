package org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

import java.util.Collections;
import java.util.Set;

public class Unreachable extends DummyNonTerminal {
	@Override
	public String toString() {
		return "unreachable";
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
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void unregister() {
	}
}