package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.query.AttributeProvider;

import java.util.HashMap;
import java.util.HashSet;

public enum CFGAnalysis implements AttributeProvider<Module, CFGAnalysisResult> {
	INSTANCE;

	private void addEdge(BasicBlock from, BasicBlock to, CFGAnalysisResult out) {
		out.successors().computeIfAbsent(from, _ -> new HashSet<>()).add(to);
		out.predecessors().computeIfAbsent(to, _ -> new HashSet<>()).add(from);
	}

	private void analysisFunction(Function function, CFGAnalysisResult out) {
		for (var basicBlock : function.getBlocks()) {
			var terminator = basicBlock.getTerminator();
			if (terminator instanceof Br br) addEdge(basicBlock, br.getTarget(), out);
			else if (terminator instanceof CondBr condBr) {
				addEdge(basicBlock, condBr.getTrueTarget(), out);
				addEdge(basicBlock, condBr.getFalseTarget(), out);
			}
		}
	}

	@Override
	public CFGAnalysisResult getAttribute(Module entity) {
		var res = new CFGAnalysisResult(new HashMap<>(), new HashMap<>());
		entity.getFunctions().values().forEach(f -> analysisFunction(f, res));
		return res;
	}
}