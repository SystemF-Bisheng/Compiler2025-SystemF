package org.systemf.compiler.ir;

import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.value.Value;

public interface AllocationSite extends Value {
	Sized valueType();
}
