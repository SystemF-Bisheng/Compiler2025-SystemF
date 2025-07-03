package org.systemf.ir.value.instruction.terminal;

import org.systemf.ir.type.Void;

public class RetVoid extends Terminal {
  public RetVoid() {
    super(new Void(), "");
  }
}
