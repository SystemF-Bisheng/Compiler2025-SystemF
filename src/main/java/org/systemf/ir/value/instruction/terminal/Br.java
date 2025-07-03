package org.systemf.ir.value.instruction.terminal;

import org.systemf.ir.block.BasicBlock;
import org.systemf.ir.type.Void;

public class Br extends Terminal {
  public Br(BasicBlock target) {
    super(new Void(), "");
    this.target = target;
  }

  public final BasicBlock target;
}
