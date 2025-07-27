package org.systemf.compiler.ir.value.instruction.nonterminal;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class DummyBinary extends DummyValueNonTerminal {
	private Value x;
	private Value y;

	protected DummyBinary(String name, Value x, Value y, Type resultType) {
		super(resultType, name);
		setX(x);
		setY(y);
	}

	public abstract String operatorName();

	@Override
	public String dumpInstructionBody() {
		return String.format("%s %s, %s", operatorName(), ValueUtil.dumpIdentifier(x), ValueUtil.dumpIdentifier(y));
	}

	public Value getX() {
		return x;
	}

	public void setX(Value x) {
		if (this.x != null) this.x.unregisterDependant(this);
		this.x = x;
		x.registerDependant(this);
	}

	public Value getY() {
		return y;
	}

	public void setY(Value y) {
		if (this.y != null) this.y.unregisterDependant(this);
		this.y = y;
		y.registerDependant(this);
	}

	@Override
	public Set<ITracked> getDependency() {
		return new HashSet<>(List.of(x, y));
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
		if (x == oldValue) setX((Value) newValue);
		if (y == oldValue) setY((Value) newValue);
	}

	@Override
	public void unregister() {
		if (x != null) x.unregisterDependant(this);
		if (y != null) y.unregisterDependant(this);
	}

	@Override
	public boolean contentEqual(Value other) {
		if (!(this.getClass() == other.getClass())) return false;
		var otherBinary = (DummyBinary) other;
		return ValueUtil.trivialInterchangeable(x, otherBinary.x) && ValueUtil.trivialInterchangeable(y, otherBinary.y);
	}
}