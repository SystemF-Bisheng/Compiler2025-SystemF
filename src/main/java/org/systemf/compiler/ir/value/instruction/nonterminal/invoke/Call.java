package org.systemf.compiler.ir.value.instruction.nonterminal.invoke;

import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.NonTerminal;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.util.ReturnTypeGetter;

public class Call extends NonTerminal {
  public Call(String name, Value func, Value... args) {
    super(ReturnTypeGetter.get(func.type), name);
    this.func = func;
    this.args = args;
  }

  public final Value func;
  public final Value[] args;
}