package org.systemf.compiler.ir.value.instruction.nonterminal.conversion;

import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.NonTerminal;

public class FptoSi extends NonTerminal {
	public final Value op;

	public FptoSi(String name, Value op) {
		super(I32.getInstance(), name);
		this.op = op;
	}
}