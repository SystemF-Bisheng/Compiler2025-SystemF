package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;

public record CFGAnalysisResult(HashMap<BasicBlock, HashSet<BasicBlock>> successors,
                                HashMap<BasicBlock, HashSet<BasicBlock>> predecessors) {
	public HashSet<BasicBlock> getSuccessors(BasicBlock block) {
		var res = successors.get(block);
		if (res == null) throw new NoSuchElementException("No such block: " + block.getName());
		return res;
	}

	public HashSet<BasicBlock> getPredecessors(BasicBlock block) {
		var res = predecessors.get(block);
		if (res == null) throw new NoSuchElementException("No such block: " + block.getName());
		return res;
	}
}