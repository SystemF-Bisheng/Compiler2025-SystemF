package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.CFGAnalysisResult;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.query.QueryManager;

import java.util.HashSet;

/**
 * Merge chains on CFG if possible
 */
public enum MergeChain implements OptPass {
	INSTANCE;

	private void mergeBlock(BasicBlock to, BasicBlock from, CFGAnalysisResult cfg) {
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

	private void handlePhi(BasicBlock cur, BasicBlock succ) {
		succ.instructions.stream().takeWhile(inst -> inst instanceof Phi).map(inst -> (Phi) inst)
				.forEach(phi -> phi.replaceAllUsage(phi.getIncoming().get(cur)));
		while (succ.getFirstInstruction() instanceof Phi phi) {
			phi.unregister();
			succ.instructions.removeFirst();
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
				var succs = cfg.successors(block);
				if (succs.size() != 1) break;
				var succ = succs.iterator().next();
				if (succ == block) break;
				if (succ == function.getEntryBlock()) break;
				var succPreds = cfg.predecessors(succ);
				if (succPreds.size() != 1) break;

				handlePhi(block, succ);

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