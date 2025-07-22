package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Collections;
import java.util.Set;

public class Ret extends DummyTerminal {
	private Value returnValue;

	public Ret(Value returnValue) {
		setReturnValue(returnValue);
	}

	@Override
	public String toString() {
		return String.format("ret %s", ValueUtil.dumpIdentifier(returnValue));
	}

	@Override
	public Set<ITracked> getDependency() {
		return Collections.singleton(returnValue);
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
		if (returnValue == oldValue) setReturnValue((Value) newValue);
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void unregister() {
		if (returnValue != null) returnValue.unregisterDependant(this);
	}

	public Value getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(Value returnValue) {
		if (this.returnValue != null) this.returnValue.unregisterDependant(this);
		this.returnValue = returnValue;
		returnValue.registerDependant(this);
	}
}