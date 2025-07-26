package org.systemf.compiler.ir.value.instruction;

/**
 * Instructions implementing this interface are potentially block sensitive,
 * which means that an instance of the instruction may behave differently when it's moved to another basic block
 */
public interface PotentialBlockSensitive extends Instruction {
}
