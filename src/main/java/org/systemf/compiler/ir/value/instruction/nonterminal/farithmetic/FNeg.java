package org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.Nonterminal;

public class FNeg extends Nonterminal {
  public FNeg(String name, Value op) {
    super(new Float(), name);
    this.op = op;
  }

  public final Value op;
}