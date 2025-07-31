package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.I64;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;

import java.util.Collections;
import java.util.Set;

public class RVLoadImm extends DummyValueNonTerminal {
	public long val;

	public RVLoadImm(long val, String name) {
		super(I64.INSTANCE, name);
		this.val = val;
	}

	@Override
	public String dumpInstructionBody() {
		return "li " + val;
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
