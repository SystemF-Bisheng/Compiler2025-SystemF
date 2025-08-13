package org.systemf.compiler.lower.rv64gc.optimization.pass;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.lower.rv64gc.analysis.RVCFGAnalysisResult;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.query.QueryManager;

import java.util.HashSet;

public enum RVRemoveSingleBr implements RVOptPass {
	INSTANCE;

	private boolean processFunction(Function function) {
		var query = QueryManager.getInstance();
		var cfg = query.getAttribute(function, RVCFGAnalysisResult.class);
		var toDel = new HashSet<BasicBlock>();
		var flag = false;
		for (var block : function.getBlocks()) {
			if (toDel.contains(block)) continue;
			if (block.instructions.size() > 1) continue;
			if (!(block.getTerminator() instanceof Br br)) continue;
			var target = br.getTarget();
			if (block == target) continue;

			var preds = cfg.predecessors(block);
			block.replaceAllUsage(target);
			preds.forEach(pred -> {
				var predSuccs = cfg.successors(pred);
				predSuccs.remove(block);
				predSuccs.add(target);
			});
			var targetPred = cfg.predecessors(target);
			targetPred.remove(block);
			targetPred.addAll(preds);
			if (block == function.getEntryBlock()) function.setEntryBlock(target);
			cfg.successors().remove(block);
			cfg.predecessors().remove(block);

			toDel.add(block);
			flag = true;
		}
		toDel.forEach(block -> {
			function.deleteBlock(block);
			block.destroy();
		});
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