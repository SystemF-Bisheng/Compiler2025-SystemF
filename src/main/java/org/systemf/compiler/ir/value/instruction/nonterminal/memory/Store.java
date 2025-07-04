package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.type.Void;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.Nonterminal;

public class Store extends Nonterminal {
  public Store(Value src, Value dest) {
    super(new Void(), "");
    this.src = src;
    this.dest = dest;
  }

  public final Value src, dest;
}