package org.systemf.compiler.lower.rv64gc.analysis;

import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.lower.rv64gc.module.position.RVRegister;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public record RVRegUsageAnalysisResult(Map<Function, Set<RVRegister>> usage) {
	public Set<RVRegister> usage(Function function) {
		return Collections.unmodifiableSet(usage.getOrDefault(function, Collections.emptySet()));
	}
}
