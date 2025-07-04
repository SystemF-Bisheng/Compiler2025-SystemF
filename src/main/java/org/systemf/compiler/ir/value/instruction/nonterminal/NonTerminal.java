package org.systemf.compiler.ir.value.instruction.nonterminal;

import org.systemf.compiler.ir.type.Type;
import org.systemf.compiler.ir.value.instruction.Instruction;

public abstract class NonTerminal extends Instruction {
  public NonTerminal(Type type, String name) {
    super(type, name);
  }
}