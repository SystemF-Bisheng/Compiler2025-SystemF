package org.systemf.compiler.ir.value.instruction.nonterminal.conversion;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;

public class SiToFp extends DummyValueNonTerminal {
	public final Value op;

	public SiToFp(String name, Value op) {
		super(Float.INSTANCE, name);
		this.op = op;
	}
}