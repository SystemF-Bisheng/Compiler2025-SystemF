package org.systemf.compiler.optimization.pass.util;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.util.Tree;

public record PositionInfo(BasicBlock block, int index) {
	public int compare(PositionInfo other, Tree<BasicBlock> domTree) {
		if (block == other.block) return Integer.compare(index, other.index);
		return Integer.compare(domTree.getDfn(block), domTree.getDfn(other.block));
	}

	public boolean dominate(PositionInfo other, Tree<BasicBlock> domTree) {
		if (block == other.block) return index <= other.index;
		return domTree.subtree(block, other.block);
	}
}
