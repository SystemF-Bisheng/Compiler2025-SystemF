package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.NonTerminal;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.util.ElementTypeGetter;

public class Load extends NonTerminal {
  public Load(String name, Value ptr) {
    super(ElementTypeGetter.get(ptr.type), name);
    this.ptr = ptr;
  }

  public final Value ptr;
}