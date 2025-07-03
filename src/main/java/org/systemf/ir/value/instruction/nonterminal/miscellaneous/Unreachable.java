package org.systemf.ir.value.instruction.nonterminal.miscellaneous;

import org.systemf.ir.type.Void;
import org.systemf.ir.value.instruction.nonterminal.Nonterminal;

public class Unreachable extends Nonterminal {
  public Unreachable() {
    super(new Void(), "");
  }
}
