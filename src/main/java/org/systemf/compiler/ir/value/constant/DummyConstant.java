package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.Type;
import org.systemf.compiler.ir.value.DummyValue;

public abstract class DummyConstant extends DummyValue implements Constant {
	protected DummyConstant(Type type) {
		super(type);
	}
}