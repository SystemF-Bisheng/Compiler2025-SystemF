package org.systemf.compiler.ir.value.instruction.nonterminal;

import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;

public abstract class DummyUnary extends DummyValueNonTerminal {
	public final Value x;

	protected DummyUnary(String name, Value x, Type resultType) {
		super(resultType, name);
		this.x = x;
	}

	public abstract String operatorName();

	@Override
	public String dumpInstructionBody() {
		return String.format("%s %s", operatorName(), ValueUtil.dumpIdentifier(x));
	}
}