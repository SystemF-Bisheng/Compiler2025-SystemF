package org.systemf.compiler.ir.value.instruction.terminal;

public class RetVoid extends DummyTerminal {
	public RetVoid() {
	}

	@Override
	public String toString() {
		return "ret void";
	}
}