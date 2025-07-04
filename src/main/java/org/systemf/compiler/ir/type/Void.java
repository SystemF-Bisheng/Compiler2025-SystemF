package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.type.util.TypeId;

public class Void extends Type {
	public Void() {
		super(TypeId.VoidId, "void");
	}

	/**
	 * a void type value can never be passed as actual parameter
	 */
	@Override
	public boolean isApplicableToFormalParameter(Type formalParameterType) {
		return false;
	}
}