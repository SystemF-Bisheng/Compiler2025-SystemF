package org.systemf.ir.type;

import org.systemf.ir.type.exception.LengthNotAvailable;
import org.systemf.ir.type.util.TypeId;

public class Array extends Type {
  /**
   * construct a variable-length array (pointer)
   */
  public Array (Type elementType) {
    super(TypeId.ArrayId, String.format("%s*", elementType.toString()));
    this.length = -1;
    this.elementType = elementType;
  }

  public Array(int length, Type elementType) {
    super(TypeId.ArrayId, String.format("[%d x %s]", length, elementType.toString()));
    assert length >= 0 : String.format("invalid array length `%d`", length);
    this.length = length;
    this.elementType = elementType;
  }

  public int getLength() throws LengthNotAvailable {
    if (isPointer()) {
      throw new LengthNotAvailable();
    }
    return length;
  }

  public boolean isPointer() {
    return length < 0;
  }

  final public Type elementType;
  final private int length;

  @Override
  public boolean isApplicableToFormalParameter(Type formalParameterType) {
    if (!(formalParameterType instanceof Array)) { return false; }
    Array formalParameterTypeArray = (Array) formalParameterType;
    return this.elementType.equals(formalParameterTypeArray.elementType);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Array)) { return false; }
    Array otherArray = (Array) other;
    return this.length == otherArray.length
      && this.elementType.equals(otherArray.elementType);
  }
}
