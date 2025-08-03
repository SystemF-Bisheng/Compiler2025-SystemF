package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.util.Tree;

import java.util.Collections;
import java.util.Map;
import java.util.SequencedSet;

public record DominanceAnalysisResult(Tree<BasicBlock> dominance,
                                      Map<BasicBlock, SequencedSet<BasicBlock>> dominanceFrontier) {
	public SequencedSet<BasicBlock> dominanceFrontier(BasicBlock block) {
		return Collections.unmodifiableSequencedSet(dominanceFrontier.get(block));
	}
}