package org.systemf.compiler.lower.rv64gc.optimization.pass;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.lower.rv64gc.analysis.RVCFGAnalysisResult;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.query.QueryManager;

import java.util.HashSet;

public enum RVMergeChain implements RVOptPass {
	INSTANCE;

	private void mergeBlock(BasicBlock to, BasicBlock from, RVCFGAnalysisResult cfg) {
		to.getLastInstruction().unregister();
		to.instructions.removeLast();
		to.instructions.addAll(from.instructions);
		from.instructions.clear();

		from.replaceAllUsage(to);
		var fromSuccs = cfg.successors(from);
		var successors = cfg.successors();
		successors.put(to, fromSuccs);
		successors.remove(from);
		cfg.predecessors().remove(from);
		for (var succ : fromSuccs) {
			var succPred = cfg.predecessors(succ);
			succPred.remove(from);
			succPred.add(to);
		}
	}

	private boolean processFunction(Function function) {
		var query = QueryManager.getInstance();
		var cfg = query.getAttribute(function, RVCFGAnalysisResult.class);
		var toDel = new HashSet<BasicBlock>();
		var flag = false;
		for (var block : function.getBlocks()) {
			if (toDel.contains(block)) continue;
			while (true) {
				var succs = cfg.successors(block);
				if (succs.size() != 1) break;
				var succ = succs.getFirst();
				if (succ == block) break;
				if (succ == function.getEntryBlock()) break;
				var succPreds = cfg.predecessors(succ);
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
	public boolean run(RVModule rvModule) {
		var module = rvModule.module();
		var res = module.getFunctions().values().stream().map(this::processFunction).reduce(false, (a, b) -> a || b);
		if (res) {
			var query = QueryManager.getInstance();
			query.invalidateAllAttributes(module);
			query.invalidateAllAttributes(rvModule);
		}
		return res;
	}
}