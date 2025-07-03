package org.systemf.ir.value.instruction.nonterminal.memory;

import org.systemf.ir.type.Void;
import org.systemf.ir.value.Value;
import org.systemf.ir.value.instruction.nonterminal.Nonterminal;

public class Store extends Nonterminal {
  public Store(Value src, Value dest) {
    super(new Void(), "");
    this.src = src;
    this.dest = dest;
  }

  public final Value src, dest;
}
