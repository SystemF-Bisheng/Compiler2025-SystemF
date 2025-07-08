package org.systemf.compiler.ir.value.instruction.nonterminal;

import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;

public abstract class DummyUnary extends DummyValueNonTerminal {
	private final Type xType;
	private Value x;

	protected DummyUnary(String name, Value x, Type xType, Type resultType) {
		super(resultType, name);
		this.xType = xType;
		setX(x);
	}

	public abstract String operatorName();

	@Override
	public String dumpInstructionBody() {
		return String.format("%s %s", operatorName(), ValueUtil.dumpIdentifier(x));
	}

	public Value getX() {
		return x;
	}

	public void setX(Value x) {
		TypeUtil.assertConvertible(x.getType(), xType, "Illegal x");
		this.x = x;
	}
}