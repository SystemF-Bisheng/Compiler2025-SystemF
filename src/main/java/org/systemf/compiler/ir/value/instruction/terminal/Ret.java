package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.type.Void;
import org.systemf.compiler.ir.value.Value;

public class Ret extends Terminal {
  public final Value returnValue;

  public Ret(Value returnValue) {
    super(new Void(), "");
    this.returnValue = returnValue;
  }
}