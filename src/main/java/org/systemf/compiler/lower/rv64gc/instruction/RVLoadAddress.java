package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.type.I64;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Collections;
import java.util.Set;

public class RVLoadAddress extends DummyValueNonTerminal {
	private GlobalVariable global;

	public RVLoadAddress(GlobalVariable global, String name) {
		super(I64.INSTANCE, name);
		setGlobal(global);
	}

	public GlobalVariable getGlobal() {
		return global;
	}

	public void setGlobal(GlobalVariable global) {
		if (this.global != null) this.global.unregisterDependant(this);
		this.global = global;
		global.registerDependant(this);
	}

	@Override
	public String dumpInstructionBody() {
		return "la " + ValueUtil.dumpIdentifier(global);
	}

	@Override
	public Set<ITracked> getDependency() {
		return Collections.singleton(global);
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
		if (oldValue == global) setGlobal((GlobalVariable) newValue);
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void unregister() {
		if (global != null) global.unregisterDependant(this);
	}
}
