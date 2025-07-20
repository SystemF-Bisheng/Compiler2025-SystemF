package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.CFGAnalysisResult;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.query.QueryManager;

import java.util.Collection;
import java.util.HashSet;

public enum RemoveSingleBr implements OptPass {
	INSTANCE;

	private boolean checkPhi(BasicBlock pred, BasicBlock block, Collection<Phi> phis) {
		return phis.stream().allMatch(phi -> {
			var incoming = phi.getIncoming();
			if (!incoming.containsKey(pred)) return true;
			return ValueUtil.trivialInterchangeable(incoming.get(pred), incoming.get(block));
		});
	}

	private void handlePhi(BasicBlock pred, BasicBlock block, Collection<Phi> phis) {
		phis.forEach(phi -> {
			var incoming = phi.getIncoming();
			if (!incoming.containsKey(pred)) phi.addIncoming(pred, incoming.get(block));
		});
		phis.forEach(phi -> phi.removeIncoming(block));
	}

	private boolean handlePhi(CFGAnalysisResult cfg, BasicBlock block, Collection<Phi> phis) {
		var preds = cfg.predecessors(block);
		var res = preds.stream().map(pred -> checkPhi(pred, block, phis)).reduce(true, Boolean::logicalAnd);
		if (res) preds.forEach(pred -> handlePhi(pred, block, phis));
		return res;
	}

	private boolean processFunction(Function function) {
		var query = QueryManager.getInstance();
		var cfg = query.getAttribute(function, CFGAnalysisResult.class);
		var toDel = new HashSet<BasicBlock>();
		var flag = false;
		for (var block : function.getBlocks()) {
			if (toDel.contains(block)) continue;
			if (block.instructions.size() > 1) continue;
			if (!(block.getTerminator() instanceof Br br)) continue;
			var target = br.getTarget();
			if (block == target) continue;
			if (target.getFirstInstruction() instanceof Phi) {
				var phis = target.instructions.stream().takeWhile(inst -> inst instanceof Phi).map(inst -> (Phi) inst)
						.toList();
				if (!handlePhi(cfg, block, phis)) continue;
			}

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
	public boolean run(Module module) {
		var res = module.getFunctions().values().stream().map(this::processFunction).reduce(false, (a, b) -> a || b);
		if (res) QueryManager.getInstance().invalidateAllAttributes(module);
		return res;
	}
}