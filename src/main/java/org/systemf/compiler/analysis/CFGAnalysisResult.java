package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public record CFGAnalysisResult(Map<BasicBlock, Set<BasicBlock>> successors,
                                Map<BasicBlock, Set<BasicBlock>> predecessors) {
	public Set<BasicBlock> successors(BasicBlock block) {
		var res = successors.get(block);
		if (res == null) throw new NoSuchElementException("No such block: " + block.getName());
		return res;
	}

	public Set<BasicBlock> predecessors(BasicBlock block) {
		var res = predecessors.get(block);
		if (res == null) throw new NoSuchElementException("No such block: " + block.getName());
		return res;
	}
}