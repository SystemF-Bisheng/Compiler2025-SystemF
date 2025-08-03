package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.query.AttributeProvider;

import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * Build control flow graph
 * <p>
 * Depend on: No
 * <p>
 * Applicable to: IR
 */
public enum CFGAnalysis implements AttributeProvider<Function, CFGAnalysisResult> {
	INSTANCE;

	private void addEdge(BasicBlock from, BasicBlock to, CFGAnalysisResult out) {
		out.successors().get(from).add(to);
		out.predecessors().get(to).add(from);
	}

	private void analyzeFunction(Function function, CFGAnalysisResult out) {
		for (var basicBlock : function.getBlocks()) {
			out.successors().put(basicBlock, new LinkedHashSet<>());
			out.predecessors().put(basicBlock, new LinkedHashSet<>());
		}
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
	public CFGAnalysisResult getAttribute(Function entity) {
		var res = new CFGAnalysisResult(new HashMap<>(), new HashMap<>());
		analyzeFunction(entity, res);
		return res;
	}
}