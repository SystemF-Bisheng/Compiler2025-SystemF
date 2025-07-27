package org.systemf.compiler.ir.value.instruction.nonterminal;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class DummyTriple extends DummyValueNonTerminal {
	private Value x;
	private Value y;
	private Value z;

	protected DummyTriple(String name, Value x, Value y, Value z, Type resultType) {
		super(resultType, name);
		setX(x);
		setY(y);
		setZ(z);
	}

	public abstract String operatorName();

	@Override
	public String dumpInstructionBody() {
		return String.format("%s %s, %s, %s", operatorName(), ValueUtil.dumpIdentifier(x), ValueUtil.dumpIdentifier(y),
				ValueUtil.dumpIdentifier(z));
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

	public Value getZ() {
		return z;
	}

	public void setZ(Value z) {
		if (this.z != null) this.z.unregisterDependant(this);
		this.z = z;
		z.registerDependant(this);
	}

	@Override
	public Set<ITracked> getDependency() {
		return new HashSet<>(List.of(x, y, z));
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
		if (x == oldValue) setX((Value) newValue);
		if (y == oldValue) setY((Value) newValue);
		if (z == oldValue) setZ((Value) newValue);
	}

	@Override
	public void unregister() {
		if (x != null) x.unregisterDependant(this);
		if (y != null) y.unregisterDependant(this);
		if (z != null) z.unregisterDependant(this);
	}

	@Override
	public boolean contentEqual(Value other) {
		if (!(this.getClass() == other.getClass())) return false;
		var otherTriple = (DummyTriple) other;
		return ValueUtil.trivialInterchangeable(x, otherTriple.x) &&
		       ValueUtil.trivialInterchangeable(y, otherTriple.y) && ValueUtil.trivialInterchangeable(z, otherTriple.z);
	}
}