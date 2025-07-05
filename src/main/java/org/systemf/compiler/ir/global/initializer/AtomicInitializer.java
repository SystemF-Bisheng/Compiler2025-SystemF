package org.systemf.compiler.ir.global.initializer;

import org.systemf.compiler.ir.value.Value;

public class AtomicInitializer implements IGlobalInitializer {
	public final Value value;

	public AtomicInitializer(Value value) {
		this.value = value;
	}
}