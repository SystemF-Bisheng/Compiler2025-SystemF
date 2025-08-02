package org.systemf.compiler.lower.rv64gc.module.register;

import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.DummyValue;
import org.systemf.compiler.lower.rv64gc.module.position.RVRegister;

public abstract class RVBuiltInRegister extends DummyValue {
	protected RVBuiltInRegister(Type type) {
		super(type);
	}

	public abstract RVRegister position();
}
