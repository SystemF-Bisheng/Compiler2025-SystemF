package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.NonTerminal;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.util.ElementTypeGetter;

public class Getptr extends NonTerminal {
  public final Value array, index;

  public Getptr(String name, Value array, Value index) {
    super(ElementTypeGetter.get(array.type), name);
    this.array = array;
    this.index = index;
  }
}