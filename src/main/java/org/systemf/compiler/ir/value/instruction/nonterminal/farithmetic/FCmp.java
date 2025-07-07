package org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

public class FCmp extends DummyValueNonTerminal {
	public final CompareOp code;
	public final Value op1, op2;

	public FCmp(String name, CompareOp code, Value op1, Value op2) {
		super(I32.INSTANCE, name);
		this.code = code;
		this.op1 = op1;
		this.op2 = op2;
	}

	@Override
	public String toString() {
		return String.format("%%%s = fcmp %s %%%s, %%%s", name, code.toString(), ValueUtil.getValueName(op1),
				ValueUtil.getValueName(op2));
	}
}