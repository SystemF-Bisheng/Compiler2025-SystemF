package org.systemf.ir.type;

import org.systemf.ir.type.util.TypeId;

public abstract class Type {
  protected Type(TypeId typeId, String typeName) {
    this.typeId = typeId;
    this.typeName = typeName;
  }

  public boolean isApplicableToFormalParameter(Type formalParameterType) {
    return this.equals(formalParameterType);
  }

  final public TypeId typeId;
  final public String typeName;

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Type)) { return false; }
    Type otherType = (Type) other;
    return this.typeId == otherType.typeId;
  }

  @Override
  public String toString() {
    return typeName;
  }
}
