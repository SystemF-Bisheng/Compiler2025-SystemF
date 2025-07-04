package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.value.Value;

public class Ret extends DummyTerminal {
	public final Value returnValue;

	public Ret(Value returnValue) {
		this.returnValue = returnValue;
	}
}