package org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous;

import org.systemf.compiler.ir.type.Void;
import org.systemf.compiler.ir.value.instruction.nonterminal.NonTerminal;

public class Unreachable extends NonTerminal {
  public Unreachable() {
    super(Void.getInstance(), "");
  }
}