package org.systemf.compiler.semantic.type;

import java.util.Arrays;

public record SysYFunction(SysYType result, SysYType... args) implements SysYType {
	@Override
	public String toString() {
		return String.format("%s(%s)", result,
				String.join(", ", Arrays.stream(args).map(SysYType::toString).toArray(String[]::new)));
	}
}