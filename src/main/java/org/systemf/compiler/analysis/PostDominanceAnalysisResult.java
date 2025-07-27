package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.util.Tree;

import java.util.Map;
import java.util.Set;

public record PostDominanceAnalysisResult(Tree<BasicBlock> dominance,
                                          Map<BasicBlock, Set<BasicBlock>> dominanceFrontier) {
}
