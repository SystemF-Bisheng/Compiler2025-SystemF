package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.CFGAnalysisResult;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.query.QueryManager;

import java.util.HashSet;

public enum MergeChain implements OptPass {
	INSTANCE;

	private void mergeBlock(BasicBlock to, BasicBlock from, CFGAnalysisResult cfg) {
		to.instructions.getLast().unregister();
		to.instructions.removeLast();
		to.instructions.addAll(from.instructions);
		from.instructions.clear();

		var fromSuccs = cfg.getSuccessors(from);
		var successors = cfg.successors();
		successors.put(to, fromSuccs);
		successors.remove(from);
		cfg.predecessors().remove(from);
		for (var succ : fromSuccs) {
			var succPred = cfg.getPredecessors(succ);
			succPred.remove(from);
			succPred.add(to);
		}
	}

	private boolean processFunction(Function function) {
		var query = QueryManager.getInstance();
		var cfg = query.getAttribute(function, CFGAnalysisResult.class);
		var toDel = new HashSet<BasicBlock>();
		boolean flag = false;
		for (var block : function.getBlocks()) {
			if (toDel.contains(block)) continue;
			while (true) {
				var succs = cfg.getSuccessors(block);
				if (succs.size() != 1) break;
				var succ = succs.iterator().next();
				if (succ == block) break;
				var succPreds = cfg.getPredecessors(succ);
				if (succPreds.size() != 1) break;

				toDel.add(succ);
				mergeBlock(block, succ, cfg);
				flag = true;
			}
		}
		toDel.forEach(function::deleteBlock);
		if (flag) query.invalidateAllAttributes(function);
		return flag;
	}

	@Override
	public boolean run(Module module) {
		var res = module.getFunctions().values().stream().map(this::processFunction).reduce(false, (a, b) -> a || b);
		if (res) QueryManager.getInstance().invalidateAllAttributes(module);
		return res;
	}
}