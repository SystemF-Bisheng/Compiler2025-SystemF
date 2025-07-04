package org.systemf.compiler.ir.value.instruction.nonterminal.invoke.util;

import org.systemf.compiler.ir.type.FunctionType;
import org.systemf.compiler.ir.type.Type;

public class ReturnTypeGetter {
	static public Type get(Type type) {
		if (!(type instanceof FunctionType func))
			throw new IllegalArgumentException("Type " + type + " is not a function");
		return func.returnType;
	}
}