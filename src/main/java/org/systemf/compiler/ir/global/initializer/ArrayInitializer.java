package org.systemf.compiler.ir.global.initializer;

public record ArrayInitializer(int length, IGlobalInitializer... elements) implements IGlobalInitializer {
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (int i = 0; i < length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(elements[i].toString());
		}
		sb.append("}");
		return sb.toString();
	}
}