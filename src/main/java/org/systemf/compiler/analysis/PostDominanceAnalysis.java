package org.systemf.compiler.analysis;

import org.systemf.compiler.analysis.util.DominanceHelper;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.terminal.IReturn;
import org.systemf.compiler.query.AttributeProvider;
import org.systemf.compiler.query.QueryManager;

import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * Depend on: CFGAnalysis
 * <p>
 * Applicable to: IR
 */
public enum PostDominanceAnalysis implements AttributeProvider<Function, PostDominanceAnalysisResult> {
	INSTANCE;

	@Override
	public PostDominanceAnalysisResult getAttribute(Function entity) {
		var cfg = QueryManager.getInstance().getAttribute(entity, CFGAnalysisResult.class);
		var virtualExit = new BasicBlock("_virtualExit");
		var exitSuccessor = new LinkedHashSet<BasicBlock>();
		var successors = new HashMap<>(cfg.predecessors());
		var predecessors = new HashMap<>(cfg.successors());

		entity.getBlocks().stream().filter(block -> block.getTerminator() instanceof IReturn).forEach(block -> {
			var newPred = new LinkedHashSet<>(predecessors.get(block));
			newPred.add(virtualExit);
			predecessors.put(block, newPred);
			exitSuccessor.add(block);
		});
		successors.put(virtualExit, exitSuccessor);
		predecessors.put(virtualExit, new LinkedHashSet<>());

		var res = DominanceHelper.analyze(virtualExit, successors, predecessors);
		return new PostDominanceAnalysisResult(res.dominance(), res.dominanceFrontier());
	}
}
