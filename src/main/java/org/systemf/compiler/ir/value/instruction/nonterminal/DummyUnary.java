package org.systemf.compiler.ir.value.instruction.nonterminal;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Collections;
import java.util.Set;

public abstract class DummyUnary extends DummyValueNonTerminal {
	private final Type xType;
	private Value x;

	protected DummyUnary(String name, Value x, Type xType, Type resultType) {
		super(resultType, name);
		this.xType = xType;
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
		TypeUtil.assertConvertible(x.getType(), xType, "Illegal x");
		if (this.x != null) this.x.unregisterDependant(this);
		this.x = x;
		x.registerDependant(this);
	}

	@Override
	public Set<ITracked> getDependency() {
		return Collections.singleton(x);
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
		if (x == oldValue) setX((Value) newValue);
	}

	@Override
	public void unregister() {
		if (x != null) x.unregisterDependant(this);
	}
}