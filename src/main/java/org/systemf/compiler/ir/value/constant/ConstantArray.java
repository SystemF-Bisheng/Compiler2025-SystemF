package org.systemf.compiler.ir.value.constant;

import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.value.Value;

public interface ConstantArray extends Value, Constant {
	Sized getElementType();

	int getSize();

	Constant getContent(int index);
}