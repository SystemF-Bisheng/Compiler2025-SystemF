package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.util.Tree;

import java.util.Map;
import java.util.SequencedSet;

public record PostDominanceAnalysisResult(Tree<BasicBlock> dominance,
                                          Map<BasicBlock, SequencedSet<BasicBlock>> dominanceFrontier) {
}
