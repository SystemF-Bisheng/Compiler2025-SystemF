package org.systemf.compiler.lower.rv64gc.analysis;

import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.lower.rv64gc.module.position.RVRegister;

import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;

public record RVRegUsageAnalysisResult(Map<Function, SortedSet<RVRegister>> usage) {
	public SortedSet<RVRegister> usage(Function function) {
		return Collections.unmodifiableSortedSet(usage.getOrDefault(function, Collections.emptySortedSet()));
	}
}
