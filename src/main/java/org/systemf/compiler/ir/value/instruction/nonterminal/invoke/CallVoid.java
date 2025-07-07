package org.systemf.compiler.ir.value.instruction.nonterminal.invoke;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Void;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;

public class CallVoid extends AbstractCall {
	public CallVoid(Value func, Value... args) {
		super(func, args);
		if (!Void.INSTANCE.equals(TypeUtil.getReturnType(func.getType())))
			throw new IllegalArgumentException("Void call inst only accepts functions returning void");
	}

	@Override
	public String toString() {
		return String.format("void call %s", dumpCallBody());
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}