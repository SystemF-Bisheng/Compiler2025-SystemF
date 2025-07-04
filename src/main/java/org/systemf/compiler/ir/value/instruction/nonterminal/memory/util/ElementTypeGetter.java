package org.systemf.compiler.ir.value.instruction.nonterminal.memory.util;

import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.type.Type;

public class ElementTypeGetter {
	static public Type get(Type type) {
		if (!(type instanceof Array arr)) {
			if (!(type instanceof Pointer ptr)) {
				throw new IllegalArgumentException("Type " + type + " is not an indexing-able");
			}
			return ptr.elementType;
		}
		return arr.elementType;
	}
}