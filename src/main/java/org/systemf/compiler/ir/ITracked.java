package org.systemf.compiler.ir;

import org.systemf.compiler.ir.value.instruction.Instruction;

import java.util.HashSet;
import java.util.Set;

public interface ITracked {
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

	default void replaceAllUsage(ITracked newValue) {
		var tmp = new HashSet<>(getDependant());
		tmp.forEach(inst -> inst.replaceAll(this, newValue));
	}

	default void replaceAllUsageExcept(ITracked newValue, Set<Instruction> except) {
		var tmp = new HashSet<>(getDependant());
		tmp.stream().filter(inst -> !except.contains(inst)).forEach(inst -> inst.replaceAll(this, newValue));
	}
}