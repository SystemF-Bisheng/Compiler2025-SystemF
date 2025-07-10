package org.systemf.compiler.ir.type;

import org.systemf.compiler.ir.type.interfaces.DummyType;
import org.systemf.compiler.ir.type.interfaces.Type;

import java.util.Arrays;
import java.util.Objects;

public class FunctionType extends DummyType {
	final public Type returnType;
	private final Type[] parameterTypes;

	public FunctionType(Type returnType, Type... parameterTypes) {
		super(typeName(returnType, parameterTypes));
		this.returnType = returnType;
		this.parameterTypes = parameterTypes;
	}

	static private String typeName(Type returnType, Type[] parameterTypes) {
		StringBuilder builder = new StringBuilder();
		builder.append(returnType.getName());
		builder.append(" (");
		for (var parameterType : parameterTypes) {
			builder.append(parameterType.getName());
			builder.append(", ");
		}
		if (parameterTypes.length > 0) { // remove trailing comma
			int length = builder.length();
			builder.delete(length - 2, length);
		}
		builder.append(")");
		return builder.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FunctionType that)) return false;
		return Objects.equals(returnType, that.returnType) && Objects.deepEquals(parameterTypes, that.parameterTypes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(returnType, Arrays.hashCode(parameterTypes));
	}

	public Type[] getParameterTypes() {
		return Arrays.copyOf(parameterTypes, parameterTypes.length);
	}
}