package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.Type;
import org.systemf.compiler.ir.value.Value;

public abstract class Constant extends Value {
  protected Constant(Type type, String name) {
    super(type, name);
  }

  @Override
  public boolean isConstant() {
    return true;
  }
}