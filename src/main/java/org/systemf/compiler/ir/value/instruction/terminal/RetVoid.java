package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.type.Void;

public class RetVoid extends Terminal {
  public RetVoid() {
    super(new Void(), "");
  }
}