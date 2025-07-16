package org.systemf.compiler.ir.value;

import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.instruction.Instruction;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class DummyValue implements Value {
	private final Set<Instruction> dependant = Collections.newSetFromMap(new WeakHashMap<>());
	final protected Type type;

	protected DummyValue(Type type) {
		this.type = type;
	}

	@Override
	public Set<Instruction> getDependant() {
		return Collections.unmodifiableSet(dependant);
	}

	@Override
	public void registerDependant(Instruction instruction) {
		dependant.add(instruction);
	}

	@Override
	public void unregisterDependant(Instruction instruction) {
		dependant.remove(instruction);
	}

	@Override
	public Type getType() {
		return type;
	}
}