package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.type.util.TypeId;

public final class I32 extends Type {
	private static I32 INSTANCE;

	private I32() {
		super(TypeId.I32Id, "int");
	}

	public static I32 getInstance() {
		if (INSTANCE == null) INSTANCE = new I32();
		return INSTANCE;
	}
}