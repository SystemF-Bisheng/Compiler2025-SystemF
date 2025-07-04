package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.INamed;

public interface Type extends INamed {
	boolean convertibleTo(Type otherType);
}