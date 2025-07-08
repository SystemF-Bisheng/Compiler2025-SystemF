package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;

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
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	public Value getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(Value returnValue) {
		this.returnValue = returnValue;
	}
}