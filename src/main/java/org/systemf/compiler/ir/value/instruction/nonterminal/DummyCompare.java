package org.systemf.compiler.ir.value.instruction.nonterminal;

import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;

public abstract class DummyCompare extends DummyBinary {
	public CompareOp method;

	public DummyCompare(String name, CompareOp method, Value x, Value y) {
		super(name, x, y, I32.INSTANCE);
		this.method = method;
	}

	@Override
	public String operatorName() {
		return String.format("%s %s", compareOperatorName(), method.name());
	}

	public abstract String compareOperatorName();

	@Override
	public boolean contentEqual(Value other) {
		if (!super.contentEqual(other)) return false;
		return method == ((DummyCompare) other).method;
	}
}