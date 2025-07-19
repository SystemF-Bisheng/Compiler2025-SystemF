package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.interfaces.Type;

import java.util.HashMap;

public class Undefined extends DummyConstant {
	private static final HashMap<Type, Undefined> INSTANCES = new HashMap<>();

	private Undefined(Type type) {
		super(type);
	}

	public static Undefined of(Type type) {
		return INSTANCES.computeIfAbsent(type, Undefined::new);
	}

	@Override
	public String toString() {
		return "undefined";
	}
}