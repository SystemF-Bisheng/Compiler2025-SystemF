package org.systemf.compiler.ir.value.instruction;

/**
 * Instructions implementing this interface potentially imply a sequence,
 * that is, adjusting the order of two such instructions may change their behavior.
 */
public interface PotentialSequential extends Instruction {
}
