package org.systemf.compiler.ir.value.instruction.nonterminal.bitwise;

import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.NonTerminal;

public class AShr extends NonTerminal {
  public final Value op1, op2;

  public AShr(String name, Value op1, Value op2) {
    super(I32.getInstance(), name);
    this.op1 = op1;
    this.op2 = op2;
  }
}