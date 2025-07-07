package org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic;

import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyCompare;

public class ICmp extends DummyCompare {
	public ICmp(String name, CompareOp method, Value x, Value y) {
		super(name, method, x, y, I32.INSTANCE);
	}

	@Override
	public String compareOperatorName() {
		return "icmp";
	}
}