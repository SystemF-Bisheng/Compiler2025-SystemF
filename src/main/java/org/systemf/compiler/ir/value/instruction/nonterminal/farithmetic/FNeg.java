package org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;

public class FNeg extends DummyValueNonTerminal {
	public final Value op;

	public FNeg(String name, Value op) {
		super(Float.INSTANCE, name);
		this.op = op;
	}
}