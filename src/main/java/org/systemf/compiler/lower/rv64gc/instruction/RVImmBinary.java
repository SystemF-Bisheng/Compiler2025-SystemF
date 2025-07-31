package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyUnary;
import org.systemf.compiler.ir.value.util.ValueUtil;

public abstract class RVImmBinary extends DummyUnary {
	public long y;

	public RVImmBinary(String name, Value x, long y, Type type) {
		super(name, x, type);
		this.y = y;
	}

	@Override
	public boolean contentEqual(Value other) {
		if (!super.contentEqual(other)) return false;
		return y == ((RVImmBinary) other).y;
	}

	@Override
	public String dumpInstructionBody() {
		return String.format("%s %s, %d", operatorName(), ValueUtil.dumpIdentifier(getX()), y);
	}
}
