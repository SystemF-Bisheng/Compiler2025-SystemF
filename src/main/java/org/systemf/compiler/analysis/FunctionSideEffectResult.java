package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.global.Function;

import java.util.Set;

public record FunctionSideEffectResult(Set<Function> sideEffectFunctions) {
	public boolean sideEffect(Function function) {
		return sideEffectFunctions.contains(function);
	}
}
