package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.PotentialBlockSensitive;
import org.systemf.compiler.ir.value.instruction.PotentialSideEffect;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.SequencedSet;

public abstract class RVStore extends DummyNonTerminal implements PotentialSideEffect, PotentialBlockSensitive {
	public long offset;
	private Value src;
	private Value dest;

	protected RVStore(Value src, Value dest, long offset) {
		setSrc(src);
		setDest(dest);
		this.offset = offset;
	}

	protected abstract String operatorName();

	@Override
	public String toString() {
		return String.format("%s %s, %s (%d)", operatorName(), ValueUtil.dumpIdentifier(src),
				ValueUtil.dumpIdentifier(dest), offset);
	}

	@Override
	public SequencedSet<ITracked> getDependency() {
		return new LinkedHashSet<>(Arrays.asList(src, dest));
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
		if (src == oldValue) setSrc((Value) newValue);
		if (dest == oldValue) setDest((Value) newValue);
	}

	@Override
	public void unregister() {
		if (src != null) src.unregisterDependant(this);
		if (dest != null) dest.unregisterDependant(this);
	}

	public Value getSrc() {
		return src;
	}

	public void setSrc(Value src) {
		if (this.src != null) this.src.unregisterDependant(this);
		this.src = src;
		src.registerDependant(this);
	}

	public Value getDest() {
		return dest;
	}

	public void setDest(Value dest) {
		if (this.dest != null) this.dest.unregisterDependant(this);
		this.dest = dest;
		dest.registerDependant(this);
	}
}
