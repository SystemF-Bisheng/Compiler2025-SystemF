package org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic;

import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

public class SDiv extends DummyNonTerminal {
	public final Value op1, op2;

	public SDiv(String name, Value op1, Value op2) {
		super(I32.INSTANCE, name);
		this.op1 = op1;
		this.op2 = op2;
	}
}