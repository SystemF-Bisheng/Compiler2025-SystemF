package org.systemf.ir.value.instruction.nonterminal.farithmetic;

import org.systemf.ir.type.Float;
import org.systemf.ir.value.Value;
import org.systemf.ir.value.instruction.nonterminal.Nonterminal;

public class FMul extends Nonterminal {
  public FMul(String name, Value op1, Value op2) {
    super(new Float(), name);
    this.op1 = op1;
    this.op2 = op2;
  }

  public final Value op1, op2;
}
