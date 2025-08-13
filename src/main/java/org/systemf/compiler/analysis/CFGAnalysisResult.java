package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SequencedSet;

public record CFGAnalysisResult(Map<BasicBlock, SequencedSet<BasicBlock>> successors,
                                Map<BasicBlock, SequencedSet<BasicBlock>> predecessors) {
	public SequencedSet<BasicBlock> successors(BasicBlock block) {
		var res = successors.get(block);
		if (res == null) throw new NoSuchElementException("No such block: " + block.getName());
		return res;
	}

	public SequencedSet<BasicBlock> predecessors(BasicBlock block) {
		var res = predecessors.get(block);
		if (res == null) throw new NoSuchElementException("No such block: " + block.getName());
		return res;
	}

	public String dumpGraph() {
		var res = new StringBuilder();
		res.append("digraph {\n");
		successors.forEach((from, tos) -> {
			res.append(String.format("\"%s\" -> {", from.getName()));
			tos.forEach(to -> res.append(String.format("\"%s\" ", to.getName())));
			res.append("}\n");
		});
		res.append('}');
		return res.toString();
	}
}