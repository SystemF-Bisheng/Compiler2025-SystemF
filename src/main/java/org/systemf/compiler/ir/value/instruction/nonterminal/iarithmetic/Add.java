package org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic;

import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.Nonterminal;

public class Add extends Nonterminal {
  public Add(String name, Value op1, Value op2) {
    super(new I32(), name);
    this.op1 = op1;
    this.op2 = op2;
  }

  public final Value op1, op2;
}