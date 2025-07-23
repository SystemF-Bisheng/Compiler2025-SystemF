package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public record LoopAnalysisResult(Map<BasicBlock, Set<BasicBlock>> loops) {
	public boolean isHead(BasicBlock block) {
		return loops.containsKey(block);
	}

	public Set<BasicBlock> getLoop(BasicBlock block) {
		return Collections.unmodifiableSet(loops.get(block));
	}
}
