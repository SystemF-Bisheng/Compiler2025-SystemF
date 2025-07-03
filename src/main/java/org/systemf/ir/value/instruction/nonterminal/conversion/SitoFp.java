package org.systemf.ir.value.instruction.nonterminal.conversion;

import org.systemf.ir.type.Float;
import org.systemf.ir.value.Value;
import org.systemf.ir.value.instruction.nonterminal.Nonterminal;

public class SitoFp extends Nonterminal {
  public SitoFp(String name, Value op) {
    super(new Float(), name);
    this.op = op;
  }

  public final Value op;
}
