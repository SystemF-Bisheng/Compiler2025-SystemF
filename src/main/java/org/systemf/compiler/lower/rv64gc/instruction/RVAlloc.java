package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.I64;
import org.systemf.compiler.ir.value.instruction.PotentialNonRepeatable;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;

import java.util.Collections;
import java.util.Set;

public class RVAlloc extends DummyValueNonTerminal implements PotentialNonRepeatable {
	public long size;
	public long alignment;

	public RVAlloc(String name, long size, long alignment) {
		super(I64.INSTANCE, name);
		this.size = size;
		this.alignment = alignment;
	}

	@Override
	public String dumpInstructionBody() {
		return String.format("alloc %d, align %d", size, alignment);
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
