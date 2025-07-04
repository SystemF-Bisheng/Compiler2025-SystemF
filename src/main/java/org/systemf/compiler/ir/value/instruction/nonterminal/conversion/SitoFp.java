package org.systemf.compiler.ir.value.instruction.nonterminal.conversion;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

public class SitoFp extends DummyNonTerminal {
	public final Value op;

	public SitoFp(String name, Value op) {
		super(Float.INSTANCE, name);
		this.op = op;
	}
}