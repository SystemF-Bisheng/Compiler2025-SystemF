package org.systemf.compiler.ir.value.instruction.nonterminal.bitwise;

import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

public class Shl extends DummyNonTerminal {
	public final Value op1, op2;

	public Shl(String name, Value op1, Value op2) {
		super(I32.INSTANCE, name);
		this.op1 = op1;
		this.op2 = op2;
	}
}