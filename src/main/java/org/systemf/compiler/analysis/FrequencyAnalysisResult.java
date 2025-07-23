package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;

import java.util.Map;

public record FrequencyAnalysisResult(Map<BasicBlock, Integer> frequency) {
	public long frequency(BasicBlock block) {
		return frequency.get(block);
	}
}
