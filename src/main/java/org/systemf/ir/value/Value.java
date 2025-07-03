package org.systemf.ir.value;

import org.systemf.ir.type.Type;
import org.systemf.ir.value.exception.NotConstant;

public abstract class Value {
  protected Value(Type type, String name) {
    this.type = type;
    this.name = name;
  }

  /**
   * for non-const value, `dump()` will return the definition of the value
   * while `toString()` will only return the representation of the value
   */
  public String dump() {
    return toString();
  }

  public boolean isConstant() {
    return false;
  }

  public long getConstantIntValue() throws NotConstant {
    throw new NotConstant();
  }

  public double getConstantFloatValue() throws NotConstant {
    throw new NotConstant();
  }

  @Override
  public String toString() {
    return name;
  }

  final public Type type;
  final public String name;
}
