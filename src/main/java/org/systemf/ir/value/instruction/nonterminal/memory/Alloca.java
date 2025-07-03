package org.systemf.ir.value.instruction.nonterminal.memory;

import org.systemf.ir.type.Array;
import org.systemf.ir.type.Type;
import org.systemf.ir.value.instruction.nonterminal.Nonterminal;

public class Alloca extends Nonterminal {
  public Alloca(String name, Type type) {
    super(new Array(type), name);
  }
}
