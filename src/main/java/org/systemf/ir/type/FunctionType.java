package org.systemf.ir.type;

import java.util.Arrays;

import org.systemf.ir.type.util.TypeId;

public class FunctionType extends Type {
  public FunctionType(Type returnType, Type... parameterTypes) {
    super(TypeId.FunctionType, typeName(returnType, parameterTypes));
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
  }

  final public Type returnType;
  final public Type[] parameterTypes;

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof FunctionType)) { return false; }
    FunctionType otherFunctionType = (FunctionType) other;
    return this.returnType.equals(otherFunctionType.returnType)
      && Arrays.equals(this.parameterTypes, otherFunctionType.parameterTypes);
  }

  static private String typeName(Type returnType, Type[] parameterTypes) {
    StringBuilder builder = new StringBuilder();
    builder.append(returnType.toString());
    builder.append(" (");
    for (Type parameterType : parameterTypes) {
      builder.append(parameterType.toString());
      builder.append(", ");
    }
    if (parameterTypes.length > 0) { // remove trailing comma
      int length = builder.length();
      builder.delete(length - 2, length);
    }
    builder.append(")");
    return builder.toString();
  }
}
