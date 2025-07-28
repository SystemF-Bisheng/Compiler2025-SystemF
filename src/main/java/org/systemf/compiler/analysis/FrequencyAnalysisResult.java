package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;

import java.util.Map;

public record FrequencyAnalysisResult(Map<BasicBlock, Integer> frequency,
                                      Map<BasicBlock, Map<BasicBlock, Integer>> distribute) {
	public int frequency(BasicBlock block) {
		return frequency.get(block);
	}

	public int distribute(BasicBlock from, BasicBlock to) {
		var tmp = distribute.get(from);
		if (tmp == null) return 0;
		return tmp.getOrDefault(to, 0);
	}
}
