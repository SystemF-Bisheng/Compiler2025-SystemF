package org.systemf.compiler.ir.value.instruction;

/**
 * Instructions that implement this interface potentially have side effects,
 * which means that they cannot be simply removed even when unused.
 */
public interface PotentialSideEffect extends Instruction {
}