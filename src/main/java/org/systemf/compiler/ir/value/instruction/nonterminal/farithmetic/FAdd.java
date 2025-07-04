package org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.NonTerminal;

public class FAdd extends NonTerminal {
	public final Value op1, op2;

	public FAdd(String name, Value op1, Value op2) {
		super(Float.getInstance(), name);
		this.op1 = op1;
		this.op2 = op2;
	}
}