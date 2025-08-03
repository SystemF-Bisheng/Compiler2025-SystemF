package org.systemf.compiler.ir.value.instruction.nonterminal;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.util.CollectionUtil;

import java.util.SequencedSet;

public abstract class DummyUnary extends DummyValueNonTerminal {
	private Value x;

	protected DummyUnary(String name, Value x, Type resultType) {
		super(resultType, name);
		setX(x);
	}

	public abstract String operatorName();

	@Override
	public String dumpInstructionBody() {
		return String.format("%s %s", operatorName(), ValueUtil.dumpIdentifier(x));
	}

	public Value getX() {
		return x;
	}

	public void setX(Value x) {
		if (this.x != null) this.x.unregisterDependant(this);
		this.x = x;
		x.registerDependant(this);
	}

	@Override
	public SequencedSet<ITracked> getDependency() {
		return CollectionUtil.singletonSequencedSet(x);
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
		if (x == oldValue) setX((Value) newValue);
	}

	@Override
	public void unregister() {
		if (x != null) x.unregisterDependant(this);
	}

	@Override
	public boolean contentEqual(Value other) {
		if (!(this.getClass() == other.getClass())) return false;
		var otherBinary = (DummyUnary) other;
		return ValueUtil.trivialInterchangeable(x, otherBinary.x);
	}
}