package org.systemf.ir.value.instruction.nonterminal.iarithmetic;

import org.systemf.ir.type.I32;
import org.systemf.ir.value.Value;
import org.systemf.ir.value.instruction.nonterminal.Nonterminal;
import org.systemf.ir.value.instruction.util.CompareCode;

public class Icmp extends Nonterminal {
  public Icmp(String name, CompareCode code, Value op1, Value op2) {
    super(new I32(), name);
    this.code = code;
    this.op1 = op1;
    this.op2 = op2;
  }

  public final CompareCode code;
  public final Value op1, op2;
}
