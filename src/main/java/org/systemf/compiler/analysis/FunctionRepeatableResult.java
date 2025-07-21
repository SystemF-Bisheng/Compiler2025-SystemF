package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.global.Function;

import java.util.Set;

public record FunctionRepeatableResult(Set<Function> nonRepeatableFunctions) {
	public boolean nonRepeatable(Function function) {
		return nonRepeatableFunctions.contains(function);
	}

	public boolean repeatable(Function function) {
		return !nonRepeatableFunctions.contains(function);
	}
}
