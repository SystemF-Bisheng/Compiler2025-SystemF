package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.Float;

public class ConstantFloat extends Constant {
  public ConstantFloat(double value) {
    super(new Float(), Double.toString(value));
    this.value = value;
  }

  @Override
  public double getConstantFloatValue()  {
    return value;
  }

  final public double value;
}