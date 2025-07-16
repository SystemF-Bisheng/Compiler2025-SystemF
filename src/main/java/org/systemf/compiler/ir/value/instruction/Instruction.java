package org.systemf.compiler.ir.value.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.value.Value;

import java.util.Set;

public interface Instruction {
	/**
	 * @return An unmodifiable set of the dependency of this instruction
	 */
	Set<Value> getDependency();

	void replaceAll(Value oldValue, Value newValue);

	<T> T accept(InstructionVisitor<T> visitor);

	/**
	 * Eagerly unregister this instruction from its dependency
	 * <p>
	 * Called on eager instruction drop
	 */
	void unregister();
}