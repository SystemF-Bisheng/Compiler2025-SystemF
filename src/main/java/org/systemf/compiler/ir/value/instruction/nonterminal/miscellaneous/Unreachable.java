package org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous;

import org.systemf.compiler.ir.type.Void;
import org.systemf.compiler.ir.value.instruction.nonterminal.Nonterminal;

public class Unreachable extends Nonterminal {
  public Unreachable() {
    super(new Void(), "");
  }
}