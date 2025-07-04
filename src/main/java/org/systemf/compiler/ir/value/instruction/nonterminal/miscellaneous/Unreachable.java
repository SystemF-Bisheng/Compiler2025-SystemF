package org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous;

import org.systemf.compiler.ir.type.Void;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

public class Unreachable extends DummyNonTerminal {
  public Unreachable() {
    super(Void.INSTANCE, "");
  }
}