package org.systemf.ir.value.instruction.terminal;

import org.systemf.ir.block.BasicBlock;
import org.systemf.ir.type.Void;
import org.systemf.ir.value.Value;

public class CondBr extends Terminal {
  public CondBr(Value cond, BasicBlock trueTarget, BasicBlock falseTarget) {
    super(new Void(), "");
    this.cond = cond;
    this.trueTarget = trueTarget;
    this.falseTarget = falseTarget;
  }

  public final Value cond;
  public final BasicBlock trueTarget, falseTarget;
}
