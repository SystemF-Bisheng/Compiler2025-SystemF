package org.systemf.compiler.lower.rv64gc.analysis;

import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.lower.rv64gc.module.position.RVRegister;

import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;

public record RVRegBackupAnalysisResult(Map<AbstractCall, SortedSet<RVRegister>> toSave,
                                        Map<Function, Integer> saveSize) {
	public SortedSet<RVRegister> toSave(AbstractCall call) {
		return Collections.unmodifiableSortedSet(toSave.getOrDefault(call, Collections.emptySortedSet()));
	}

	public int saveSize(Function function) {
		return saveSize.getOrDefault(function, 0);
	}
}
