package org.systemf.compiler.lower.rv64gc.analysis;

import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public record RVLiveRangeAnalysisResult(Map<Instruction, Set<Value>> aliveBefore) {
	public Set<Value> aliveBefore(Instruction instruction) {
		return Collections.unmodifiableSet(aliveBefore.getOrDefault(instruction, Collections.emptySet()));
	}
}
