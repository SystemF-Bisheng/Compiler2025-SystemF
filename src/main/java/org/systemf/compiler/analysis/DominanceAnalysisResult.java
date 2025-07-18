package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.util.Tree;

public record DominanceAnalysisResult(Tree<BasicBlock> dominance) {
}