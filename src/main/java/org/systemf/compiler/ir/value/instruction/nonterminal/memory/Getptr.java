package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.Nonterminal;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.util.ElementTypeGetter;

public class Getptr extends Nonterminal {
  public Getptr(String name, Value array, Value index) {
    super(ElementTypeGetter.get(array.type), name);
    this.array = array;
    this.index = index;
  }

  public final Value array, index;
}