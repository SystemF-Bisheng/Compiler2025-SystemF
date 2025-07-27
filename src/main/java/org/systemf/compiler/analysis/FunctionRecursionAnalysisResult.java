package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.global.Function;

import java.util.Set;

public record FunctionRecursionAnalysisResult(Set<Function> recursive) {
	public boolean recursive(Function function) {
		return recursive.contains(function);
	}
}
