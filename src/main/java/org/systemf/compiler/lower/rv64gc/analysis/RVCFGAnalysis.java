package org.systemf.compiler.lower.rv64gc.analysis;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.lower.rv64gc.instruction.RVCompBranch;
import org.systemf.compiler.query.AttributeProvider;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Build control flow graph
 * <p>
 * Depend on: No
 * <p>
 * Applicable to: RV64GC
 */
public enum RVCFGAnalysis implements AttributeProvider<Function, RVCFGAnalysisResult> {
	INSTANCE;

	private void addEdge(BasicBlock from, BasicBlock to, RVCFGAnalysisResult out) {
		out.successors().get(from).add(to);
		out.predecessors().get(to).add(from);
	}

	private void analyzeFunction(Function function, RVCFGAnalysisResult out) {
		for (var basicBlock : function.getBlocks()) {
			out.successors().put(basicBlock, new HashSet<>());
			out.predecessors().put(basicBlock, new HashSet<>());
		}
		for (var basicBlock : function.getBlocks()) {
			var terminator = basicBlock.getTerminator();
			if (terminator instanceof Br br) addEdge(basicBlock, br.getTarget(), out);
			else if (terminator instanceof RVCompBranch compBr) {
				addEdge(basicBlock, compBr.getTrueTarget(), out);
				addEdge(basicBlock, compBr.getFalseTarget(), out);
			}
		}
	}

	@Override
	public RVCFGAnalysisResult getAttribute(Function entity) {
		var res = new RVCFGAnalysisResult(new HashMap<>(), new HashMap<>());
		analyzeFunction(entity, res);
		return res;
	}
}