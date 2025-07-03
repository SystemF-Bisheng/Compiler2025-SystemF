package org.systemf.ir.value.constant;

import org.systemf.ir.type.Type;
import org.systemf.ir.value.Value;

public abstract class Constant extends Value {
  protected Constant(Type type, String name) {
    super(type, name);
  }

  @Override
  public boolean isConstant() {
    return true;
  }
}
