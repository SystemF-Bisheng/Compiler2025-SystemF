package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.type.Void;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.NonTerminal;

public class Store extends NonTerminal {
  public final Value src, dest;

  public Store(Value src, Value dest) {
    super(Void.getInstance(), "");
    this.src = src;
    this.dest = dest;
  }
}