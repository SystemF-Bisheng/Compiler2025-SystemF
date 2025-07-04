package org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.NonTerminal;

public class FNeg extends NonTerminal {
  public final Value op;

  public FNeg(String name, Value op) {
    super(new Float(), name);
    this.op = op;
  }
}