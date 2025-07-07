package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;

public class Ret extends DummyTerminal {
	public final Value returnValue;

	public Ret(Value returnValue) {
		this.returnValue = returnValue;
	}

	@Override
	public String toString() {
		return String.format("ret %s", ValueUtil.dumpIdentifier(returnValue));
	}
}