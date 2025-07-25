package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.query.AttributeProvider;
import org.systemf.compiler.query.QueryManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum ReachabilityAnalysis implements AttributeProvider<Function, ReachabilityAnalysisResult> {
	INSTANCE;

	private void dfs(CFGAnalysisResult cfg, BasicBlock cur, Set<BasicBlock> visited) {
		if (!visited.add(cur)) return;
		for (var nxt : cfg.successors(cur)) dfs(cfg, nxt, visited);
	}

	private Set<BasicBlock> analyzeBlock(CFGAnalysisResult cfg, BasicBlock block) {
		var res = new HashSet<BasicBlock>();
		dfs(cfg, block, res);
		return res;
	}

	@Override
	public ReachabilityAnalysisResult getAttribute(Function entity) {
		Map<BasicBlock, Set<BasicBlock>> reachable = new HashMap<>();
		var cfg = QueryManager.getInstance().getAttribute(entity, CFGAnalysisResult.class);
		entity.getBlocks().forEach(block -> reachable.put(block, analyzeBlock(cfg, block)));
		return new ReachabilityAnalysisResult(reachable);
	}
}
