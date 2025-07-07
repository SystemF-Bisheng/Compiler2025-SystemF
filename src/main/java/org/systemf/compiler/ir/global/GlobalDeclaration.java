package org.systemf.compiler.ir.global;

import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.global.initializer.IGlobalInitializer;
import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.DummyValue;

public class GlobalDeclaration extends DummyValue implements IGlobal, INamed {
	public final IGlobalInitializer initializer;
	private final String name;

	public GlobalDeclaration(String name, Type type, IGlobalInitializer initializer) {
		super(new Pointer(type));
		this.name = name;
		this.initializer = initializer;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return String.format("@%s = global %s %s", name, type.getName(), initializer.toString());
	}
}