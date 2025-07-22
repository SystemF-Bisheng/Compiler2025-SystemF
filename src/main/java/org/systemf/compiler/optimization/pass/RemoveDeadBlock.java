package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.CFGAnalysisResult;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.query.QueryManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum RemoveDeadBlock implements OptPass {
	INSTANCE;

	private void dfs(BasicBlock cur, Set<BasicBlock> toDel, Map<BasicBlock, Set<BasicBlock>> successor) {
		if (!toDel.contains(cur)) return;
		toDel.remove(cur);
		for (var succ : successor.get(cur)) dfs(succ, toDel, successor);
	}

	private boolean processFunction(Function function) {
		var query = QueryManager.getInstance();
		var cfg = query.getAttribute(function, CFGAnalysisResult.class);
		var toDel = new HashSet<>(function.getBlocks());
		dfs(function.getEntryBlock(), toDel, cfg.successors());
		for (var del : toDel) {
			function.deleteBlock(del);
			del.destroy();
		}

		if (toDel.isEmpty()) return false;
		query.invalidateAllAttributes(function);
		return true;
	}

	@Override
	public boolean run(Module module) {
		var changed = module.getFunctions().values().stream().map(this::processFunction)
				.reduce(false, (a, b) -> a || b);
		if (changed) QueryManager.getInstance().invalidateAllAttributes(module);
		return changed;
	}
}