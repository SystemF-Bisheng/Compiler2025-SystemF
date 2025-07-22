package org.systemf.compiler.ir.value.instruction;

import org.systemf.compiler.ir.value.Value;

/**
 * Instructions that implement this interface are potentially non-repeatable, which means different instances of the instruction may yield different values even when they have trivially interchangeable arguments.
 */
public interface PotentialNonRepeatable extends Instruction, Value {
}
