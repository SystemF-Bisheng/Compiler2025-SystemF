package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;

import java.util.Map;
import java.util.Set;

public record ReachabilityAnalysisResult(Map<BasicBlock, Set<BasicBlock>> reachable) {
	public boolean reachable(BasicBlock from, BasicBlock to) {
		return reachable.get(from).contains(to);
	}
}
