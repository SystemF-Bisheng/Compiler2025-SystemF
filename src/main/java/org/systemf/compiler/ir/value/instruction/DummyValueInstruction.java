package org.systemf.compiler.ir.value.instruction;

import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.type.Type;
import org.systemf.compiler.ir.value.DummyValue;
import org.systemf.compiler.ir.value.Value;

public abstract class DummyValueInstruction extends DummyValue implements Value, Instruction, INamed {
	protected final String name;

	protected DummyValueInstruction(Type type, String name) {
		super(type);
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}