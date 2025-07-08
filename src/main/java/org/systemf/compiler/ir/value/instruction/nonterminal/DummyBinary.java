package org.systemf.compiler.ir.value.instruction.nonterminal;

import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;

public abstract class DummyBinary extends DummyValueNonTerminal {
	public final Value x;
	public final Value y;

	protected DummyBinary(String name, Value x, Value y, Type xType, Type yType, Type resultType) {
		super(resultType, name);
		TypeUtil.assertSameType(x.getType(), xType, "Illegal x");
		TypeUtil.assertSameType(y.getType(), yType, "Illegal y");
		this.x = x;
		this.y = y;
	}

	public abstract String operatorName();

	@Override
	public String dumpInstructionBody() {
		return String.format("%s %s, %s", operatorName(), ValueUtil.dumpIdentifier(x), ValueUtil.dumpIdentifier(y));
	}
}