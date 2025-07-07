package org.systemf.compiler.ir.value.instruction;

import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.DummyValue;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;

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

	public abstract String dumpInstructionBody();

	@Override
	public String toString() {
		return String.format("%s = %s", ValueUtil.dumpIdentifier(this), dumpInstructionBody());
	}
}