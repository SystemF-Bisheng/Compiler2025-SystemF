package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.type.util.TypeId;

public final class Void extends Type {
	private static Void INSTANCE;

	private Void() {
		super(TypeId.VoidId, "void");
	}

	/**
	 * a void type value can never be passed as actual parameter
	 */
	@Override
	public boolean isApplicableToFormalParameter(Type formalParameterType) {
		return false;
	}

	public static Void getInstance() {
		if (INSTANCE == null) INSTANCE = new Void();
		return INSTANCE;
	}
}