package org.systemf.compiler.ir.value.instruction.nonterminal.invoke;

import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Void;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;

public class Call extends AbstractCall implements Value, INamed {
	private final String name;
	private final Type type;

	public Call(String name, Value func, Value... args) {
		super(func, args);
		this.name = name;
		this.type = TypeUtil.getReturnType(func.getType());
		if (Void.INSTANCE.equals(type))
			throw new IllegalArgumentException("Valued call inst doesn't accept functions returning void");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format("%s = call %s", ValueUtil.dumpIdentifier(this), dumpCallBody());
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}