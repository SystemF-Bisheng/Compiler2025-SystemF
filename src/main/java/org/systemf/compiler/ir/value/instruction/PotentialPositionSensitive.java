package org.systemf.compiler.ir.value.instruction;

/**
 * Instructions implementing this interface are potentially position sensitive,
 * which means that an instance of the instruction may behave differently when it's moved to another position
 */
public interface PotentialPositionSensitive extends Instruction {
}
