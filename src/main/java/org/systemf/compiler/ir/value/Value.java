package org.systemf.compiler.ir.value;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.type.interfaces.Type;

public interface Value extends ITracked {
	Type getType();
}