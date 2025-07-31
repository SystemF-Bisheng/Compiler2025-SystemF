package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.interfaces.Sized;

import java.util.HashMap;

public class Undefined extends DummyConstant {
	private static final HashMap<Sized, Undefined> INSTANCES = new HashMap<>();

	private Undefined(Sized type) {
		super(type);
	}

	public static Undefined of(Sized type) {
		return INSTANCES.computeIfAbsent(type, Undefined::new);
	}

	@Override
	public String toString() {
		return "undefined";
	}
}