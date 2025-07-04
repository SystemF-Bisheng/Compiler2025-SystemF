package org.systemf.compiler.ir.value;

import org.systemf.compiler.ir.type.Type;
import org.systemf.compiler.ir.value.exception.NonConstantException;

public abstract class Value {
	final public Type type;
	final public String name;

	protected Value(Type type, String name) {
		this.type = type;
		this.name = name;
	}

	/**
	 * for non-const value, `dump()` will return the definition of the value
	 * while `toString()` will only return the representation of the value
	 */
	public String dump() {
		return toString();
	}

	public boolean isConstant() {
		return false;
	}

	public long getConstantIntValue() throws NonConstantException {
		throw new NonConstantException();
	}

	public double getConstantFloatValue() throws NonConstantException {
		throw new NonConstantException();
	}

	@Override
	public String toString() {
		return name;
	}
}