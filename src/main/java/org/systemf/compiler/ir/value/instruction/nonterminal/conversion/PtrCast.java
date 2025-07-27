package org.systemf.compiler.ir.value.instruction.nonterminal.conversion;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Collections;
import java.util.Set;

public class PtrCast extends DummyValueNonTerminal {
	private Value x;

	public PtrCast(String name, Value x, Type resultType) {
		super(resultType, name);
		setX(x);
	}

	@Override
	public String dumpInstructionBody() {
		return String.format("ptrcast %s %s", type, ValueUtil.dumpIdentifier(x));
	}

	public Value getX() {
		return x;
	}

	public void setX(Value x) {
		var xType = x.getType();
		if (!(xType instanceof Pointer)) throw new IllegalArgumentException("x is not a pointer");
		TypeUtil.assertConvertible(xType, type, "Illegal x");
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
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void unregister() {
		if (x != null) x.unregisterDependant(this);
	}

	@Override
	public boolean contentEqual(Value other) {
		if (!(other instanceof PtrCast otherCast)) return false;
		return type.equals(otherCast.type) && ValueUtil.trivialInterchangeable(x, otherCast.x);
	}
}
