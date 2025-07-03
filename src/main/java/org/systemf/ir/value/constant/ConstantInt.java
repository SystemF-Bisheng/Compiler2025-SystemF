package org.systemf.ir.value.constant;

import org.systemf.ir.type.I32;

public class ConstantInt extends Constant {
  public ConstantInt(long value) {
    super(new I32(), Long.toString(value));
    this.value = value;
  }

  @Override
  public long getConstantIntValue() {
    return value;
  }

  final public long value;
}
