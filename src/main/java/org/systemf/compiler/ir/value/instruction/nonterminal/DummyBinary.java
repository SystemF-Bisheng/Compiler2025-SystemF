package org.systemf.compiler.ir.value.instruction.nonterminal;

import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;

public abstract class DummyBinary extends DummyValueNonTerminal {
	private final Type xType;
	private final Type yType;
	private Value x;
	private Value y;

	protected DummyBinary(String name, Value x, Value y, Type xType, Type yType, Type resultType) {
		super(resultType, name);
		this.xType = xType;
		this.yType = yType;
		setX(x);
		setY(y);
	}

	public abstract String operatorName();

	@Override
	public String dumpInstructionBody() {
		return String.format("%s %s, %s", operatorName(), ValueUtil.dumpIdentifier(x), ValueUtil.dumpIdentifier(y));
	}

	public Value getX() {
		return x;
	}

	public void setX(Value x) {
		TypeUtil.assertConvertible(x.getType(), xType, "Illegal x");
		this.x = x;
	}

	public Value getY() {
		return y;
	}

	public void setY(Value y) {
		TypeUtil.assertConvertible(y.getType(), yType, "Illegal y");
		this.y = y;
	}
}