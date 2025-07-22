package org.systemf.compiler.ir.value.instruction;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;

import java.util.Set;

public interface Instruction {
	/**
	 * @return An unmodifiable set of the dependency of this instruction
	 */
	Set<ITracked> getDependency();

	void replaceAll(ITracked oldValue, ITracked newValue);

	<T> T accept(InstructionVisitor<T> visitor);

	/**
	 * Eagerly unregister this instruction from its dependency
	 * <p>
	 * Called on eager instruction drop
	 */
	void unregister();
}