package org.systemf.compiler.ir.type.util;

import org.systemf.compiler.ir.type.FunctionType;
import org.systemf.compiler.ir.type.interfaces.Dereferenceable;
import org.systemf.compiler.ir.type.interfaces.Sized;
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
		return func.getParameterTypes();
	}

	static public Type getElementType(Type type) {
		if (!(type instanceof Dereferenceable der))
			throw new IllegalArgumentException("Type " + type + " is not dereferenceable");
		return der.getElementType();
	}

	public static void assertConvertible(Type given, Type expected, String message) {
		if (!given.convertibleTo(expected)) throw new IllegalArgumentException(
				String.format("%s: the given type %s isn't convertible to the expected type %s", message, given,
						expected));
	}

	public static Sized assertSized(Type given, String message) {
		if (given instanceof Sized) return (Sized) given;
		throw new IllegalArgumentException(String.format("%s: the given type %s is not sized", message, given));
	}
}