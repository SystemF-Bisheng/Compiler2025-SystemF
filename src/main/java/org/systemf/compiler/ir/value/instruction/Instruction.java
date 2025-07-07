package org.systemf.compiler.ir.value.instruction;

import org.systemf.compiler.ir.InstructionVisitor;

public interface Instruction {
	public abstract <T> T accept(InstructionVisitor<T> visitor);
}