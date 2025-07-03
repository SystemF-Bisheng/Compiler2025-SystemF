package org.systemf.ir.value.instruction.nonterminal.conversion;

import org.systemf.ir.type.I32;
import org.systemf.ir.value.Value;
import org.systemf.ir.value.instruction.nonterminal.Nonterminal;

public class FptoSi extends Nonterminal {
  public FptoSi(String name, Value op) {
    super(new I32(), name);
    this.op = op;
  }

  public final Value op;
}
