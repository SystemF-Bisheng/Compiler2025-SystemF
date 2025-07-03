package org.systemf.ir.value.instruction.nonterminal.invoke;

import org.systemf.ir.value.Value;
import org.systemf.ir.value.instruction.nonterminal.Nonterminal;
import org.systemf.ir.value.instruction.nonterminal.invoke.util.ReturnTypeGetter;

public class Call extends Nonterminal {
  public Call(String name, Value func, Value... args) {
    super(ReturnTypeGetter.get(func.type), name);
    this.func = func;
    this.args = args;
  }

  public final Value func;
  public final Value[] args;
}
