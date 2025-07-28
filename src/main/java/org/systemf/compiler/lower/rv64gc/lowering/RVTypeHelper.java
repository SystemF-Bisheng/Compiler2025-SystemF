package org.systemf.compiler.lower.rv64gc.lowering;

import org.systemf.compiler.ir.type.*;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.interfaces.Type;

import java.util.Arrays;

public class RVTypeHelper {
	public static Type lowerType(Type type) {
		if (type instanceof Pointer) return I64.INSTANCE;
		return type;
	}

	public static FunctionType lowerFunctionType(FunctionType functionType) {
		return new FunctionType(lowerType(functionType.returnType),
				Arrays.stream(functionType.getParameterTypes()).map(RVTypeHelper::lowerType).toArray(Type[]::new));
	}

	public static long sizeOf(Sized sized) {
		if (sized instanceof I32) return 4;
		else if (sized instanceof I64) return 8;
		else if (sized instanceof Float) return 4;
		else if (sized instanceof Pointer) return 8;
		else if (sized instanceof Array arr) return arr.length * sizeOf(arr.getElementType());
		throw new UnsupportedOperationException();
	}

	public static long alignmentOf(Sized sized) {
		if (sized instanceof I32) return 4;
		else if (sized instanceof I64) return 8;
		else if (sized instanceof Float) return 4;
		else if (sized instanceof Pointer) return 8;
		else if (sized instanceof Array arr) return alignmentOf(arr.getElementType());
		throw new UnsupportedOperationException();
	}
}
