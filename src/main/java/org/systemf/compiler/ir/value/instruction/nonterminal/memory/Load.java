package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.util.ElementTypeGetter;

public class Load extends DummyNonTerminal {
  public final Value ptr;

  public Load(String name, Value ptr) {
    super(ElementTypeGetter.get(ptr.getType()), name);
    this.ptr = ptr;
  }
}