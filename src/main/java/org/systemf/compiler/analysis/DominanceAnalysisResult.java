package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.util.Tree;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public record DominanceAnalysisResult(Tree<BasicBlock> dominance, Map<BasicBlock, Set<BasicBlock>> dominanceFrontier) {
	public Set<BasicBlock> dominanceFrontier(BasicBlock block) {
		return Collections.unmodifiableSet(dominanceFrontier.get(block));
	}
}