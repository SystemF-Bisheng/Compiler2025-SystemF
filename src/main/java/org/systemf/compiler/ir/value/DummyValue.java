package org.systemf.compiler.ir.value;

import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.instruction.Instruction;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class DummyValue implements Value {
	final protected Type type;
	private final Map<Instruction, Integer> dependant = new WeakHashMap<>();

	protected DummyValue(Type type) {
		this.type = type;
	}

	@Override
	public Set<Instruction> getDependant() {
		return Collections.unmodifiableSet(dependant.keySet());
	}

	@Override
	public void registerDependant(Instruction instruction) {
		dependant.compute(instruction, (_, cnt) -> cnt == null ? 1 : cnt + 1);
	}

	@Override
	public void unregisterDependant(Instruction instruction) {
		dependant.compute(instruction, (_, cnt) -> {
			if (cnt == null) return null;
			if (cnt == 1) return null;
			return cnt - 1;
		});
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public boolean contentEqual(Value other) {
		return this == other;
	}
}