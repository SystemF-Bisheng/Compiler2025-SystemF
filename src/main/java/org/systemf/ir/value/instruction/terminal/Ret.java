package org.systemf.ir.value.instruction.terminal;

import org.systemf.ir.type.Void;
import org.systemf.ir.value.Value;

public class Ret extends Terminal {
  public Ret(Value returnValue) {
    super(new Void(), "");
    this.returnValue = returnValue;
  }

  public final Value returnValue;
}
