package org.systemf.ir.value.instruction.nonterminal.memory;

import org.systemf.ir.value.Value;
import org.systemf.ir.value.instruction.nonterminal.Nonterminal;
import org.systemf.ir.value.instruction.nonterminal.memory.util.ElementTypeGetter;

public class Load extends Nonterminal {
  public Load(String name, Value ptr) {
    super(ElementTypeGetter.get(ptr.type), name);
    this.ptr = ptr;
  }

  public final Value ptr;
}
