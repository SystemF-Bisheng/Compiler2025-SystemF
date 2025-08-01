package org.systemf.compiler.lower.rv64gc.analysis;

import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public record RVLiveRangeAnalysisResult(Map<Instruction, Set<Value>> aliveBefore,
                                        Map<Value, Set<Instruction>> aliveBeforeInst,
                                        Map<Instruction, Set<Value>> aliveAfter) {
	public Set<Value> aliveBefore(Instruction instruction) {
		return Collections.unmodifiableSet(aliveBefore.getOrDefault(instruction, Collections.emptySet()));
	}

	public Set<Instruction> aliveBeforeInst(Value value) {
		return Collections.unmodifiableSet(aliveBeforeInst.getOrDefault(value, Collections.emptySet()));
	}

	public Set<Value> aliveAfter(Instruction instruction) {
		return Collections.unmodifiableSet(aliveAfter.getOrDefault(instruction, Collections.emptySet()));
	}
}
