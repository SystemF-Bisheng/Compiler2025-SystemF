package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.PotentialBlockSensitive;
import org.systemf.compiler.ir.value.instruction.PotentialNonRepeatable;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Collections;
import java.util.Set;

public abstract class RVLoad extends DummyValueNonTerminal implements PotentialNonRepeatable, PotentialBlockSensitive {
	private Value ptr;
	public long offset;

	protected RVLoad(String name, Type type, Value ptr, long offset) {
		super(type, name);
		setPointer(ptr);
		this.offset = offset;
	}

	protected abstract String operatorName();

	@Override
	public String dumpInstructionBody() {
		return String.format("%s %s (%d)", operatorName(), ValueUtil.dumpIdentifier(ptr), offset);
	}

	@Override
	public Set<ITracked> getDependency() {
		return Collections.singleton(ptr);
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
		if (ptr == oldValue) setPointer((Value) newValue);
	}

	@Override
	public void unregister() {
		if (ptr != null) ptr.unregisterDependant(this);
	}

	public Value getPointer() {
		return ptr;
	}

	public void setPointer(Value ptr) {
		if (this.ptr != null) this.ptr.unregisterDependant(this);
		this.ptr = ptr;
		ptr.registerDependant(this);
	}

	@Override
	public boolean contentEqual(Value other) {
		if (getClass() != other.getClass()) return false;
		return ValueUtil.trivialInterchangeable(ptr, ((RVLoad) other).ptr);
	}
}
