package org.systemf.compiler.ir.type.util;

import org.systemf.compiler.ir.type.FunctionType;
import org.systemf.compiler.ir.type.interfaces.Indexable;
import org.systemf.compiler.ir.type.interfaces.Type;

public class TypeUtil {
	static public Type getReturnType(Type type) {
		if (!(type instanceof FunctionType func))
			throw new IllegalArgumentException("Type " + type + " is not a function");
		return func.returnType;
	}

	static public Type[] getParameterTypes(Type type) {
		if (!(type instanceof FunctionType func))
			throw new IllegalArgumentException("Type " + type + " is not a function");
		return func.parameterTypes;
	}

	static public Type getElementType(Type type) {
		if (!(type instanceof Indexable ind)) throw new IllegalArgumentException("Type " + type + " is not indexable");
		return ind.getElementType();
	}
}