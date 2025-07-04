package org.systemf.compiler.ir.value.instruction.nonterminal.conversion;

import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.NonTerminal;

public class SitoFp extends NonTerminal {
  public final Value op;

  public SitoFp(String name, Value op) {
    super(new Float(), name);
    this.op = op;
  }
}