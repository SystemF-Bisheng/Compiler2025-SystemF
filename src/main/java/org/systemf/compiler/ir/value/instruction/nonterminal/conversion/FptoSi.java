package org.systemf.compiler.ir.value.instruction.nonterminal.conversion;

import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.Nonterminal;

public class FptoSi extends Nonterminal {
  public FptoSi(String name, Value op) {
    super(new I32(), name);
    this.op = op;
  }

  public final Value op;
}