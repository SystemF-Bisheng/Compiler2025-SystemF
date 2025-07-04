package org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

public class FCmp extends DummyNonTerminal {
	public final CompareOp code;
	public final Value op1, op2;

	public FCmp(String name, CompareOp code, Value op1, Value op2) {
		super(I32.INSTANCE, name);
		this.code = code;
		this.op1 = op1;
		this.op2 = op2;
	}
}