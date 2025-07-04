package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.Type;
import org.systemf.compiler.ir.value.instruction.nonterminal.Nonterminal;

public class Alloca extends Nonterminal {
  public Alloca(String name, Type type) {
    super(new Array(type), name);
  }
}