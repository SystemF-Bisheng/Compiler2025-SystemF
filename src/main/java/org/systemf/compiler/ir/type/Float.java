package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.type.util.TypeId;

public final class Float extends Type {
	private static Float INSTANCE;

	private Float() {
		super(TypeId.FloatId, "float");
	}

	public static Float getInstance() {
		if (INSTANCE == null) INSTANCE = new Float();
		return INSTANCE;
	}
}