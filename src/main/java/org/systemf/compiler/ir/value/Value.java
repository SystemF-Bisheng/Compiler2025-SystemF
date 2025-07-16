package org.systemf.compiler.ir.value;

import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.instruction.Instruction;

import java.util.Set;

public interface Value {
	/**
	 * @return An unmodifiable set of instructions depending on this value
	 */
	Set<Instruction> getDependant();

	/**
	 * @param instruction A new instruction depending on this value
	 */
	void registerDependant(Instruction instruction);

	/**
	 * @param instruction An instruction that no longer depends on this value
	 */
	void unregisterDependant(Instruction instruction);

	Type getType();
}