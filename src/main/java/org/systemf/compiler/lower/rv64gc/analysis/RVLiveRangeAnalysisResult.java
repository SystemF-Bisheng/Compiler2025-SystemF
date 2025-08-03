package org.systemf.compiler.lower.rv64gc.analysis;

import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;

import java.util.Collections;
import java.util.Map;
import java.util.SequencedSet;

public record RVLiveRangeAnalysisResult(Map<Instruction, SequencedSet<Value>> aliveBefore,
                                        Map<Value, SequencedSet<Instruction>> aliveBeforeInst,
                                        Map<Instruction, SequencedSet<Value>> aliveAfter) {
	public SequencedSet<Value> aliveBefore(Instruction instruction) {
		return Collections.unmodifiableSequencedSet(
				aliveBefore.getOrDefault(instruction, Collections.emptySortedSet()));
	}

	public SequencedSet<Instruction> aliveBeforeInst(Value value) {
		return Collections.unmodifiableSequencedSet(aliveBeforeInst.getOrDefault(value, Collections.emptySortedSet()));
	}

	public SequencedSet<Value> aliveAfter(Instruction instruction) {
		return Collections.unmodifiableSequencedSet(aliveAfter.getOrDefault(instruction, Collections.emptySortedSet()));
	}
}
