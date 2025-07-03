package org.systemf.ir.value.instruction.nonterminal.iarithmetic;

import org.systemf.ir.type.I32;
import org.systemf.ir.value.Value;
import org.systemf.ir.value.instruction.nonterminal.Nonterminal;

public class SDiv extends Nonterminal {
  public SDiv(String name, Value op1, Value op2) {
    super(new I32(), name);
    this.op1 = op1;
    this.op2 = op2;
  }

  public final Value op1, op2;
}
