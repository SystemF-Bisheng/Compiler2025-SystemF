package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.type.util.TypeId;

public abstract class Type {
	final public TypeId typeId;
	final public String typeName;

	protected Type(TypeId typeId, String typeName) {
		this.typeId = typeId;
		this.typeName = typeName;
	}

	public boolean isApplicableToFormalParameter(Type formalParameterType) {
		return this.equals(formalParameterType);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Type otherType)) {return false;}
		return this.typeId == otherType.typeId;
	}

	@Override
	public String toString() {
		return typeName;
	}
}