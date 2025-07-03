package org.systemf.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.ir.type.Float;
import org.systemf.ir.value.Value;
import org.systemf.ir.value.instruction.nonterminal.Nonterminal;

public class FNeg extends Nonterminal {
  public FNeg(String name, Value op) {
    super(new Float(), name);
    this.op = op;
  }

  public final Value op;
}
