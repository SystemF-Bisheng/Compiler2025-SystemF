package org.systemf.compiler.ir.value.instruction.nonterminal;

import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;

public abstract class DummyCompare extends DummyBinary {
	public final CompareOp method;

	public DummyCompare(String name, CompareOp method, Value x, Value y, Type xType, Type yType, Type resultType) {
		super(name, x, y, xType, yType, resultType);
		this.method = method;
	}

	@Override
	public String operatorName() {
		return String.format("%s %s", compareOperatorName(), method.name());
	}

	public abstract String compareOperatorName();
}