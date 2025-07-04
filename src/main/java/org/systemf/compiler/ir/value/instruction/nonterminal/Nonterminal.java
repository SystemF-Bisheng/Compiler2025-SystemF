package org.systemf.compiler.ir.value.instruction.nonterminal;

import org.systemf.compiler.ir.type.Type;
import org.systemf.compiler.ir.value.instruction.Instruction;

public class Nonterminal extends Instruction {
  public Nonterminal(Type type, String name) {
    super(type, name);
  }
}