package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.type.Void;

public class Br extends Terminal {
  public Br(BasicBlock target) {
    super(new Void(), "");
    this.target = target;
  }

  public final BasicBlock target;
}