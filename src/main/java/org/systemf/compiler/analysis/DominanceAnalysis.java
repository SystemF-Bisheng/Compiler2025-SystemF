package org.systemf.compiler.analysis;

import org.systemf.compiler.analysis.util.DominanceHelper;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.query.AttributeProvider;
import org.systemf.compiler.query.QueryManager;

public enum DominanceAnalysis implements AttributeProvider<Function, DominanceAnalysisResult> {
	INSTANCE;

	@Override
	public DominanceAnalysisResult getAttribute(Function entity) {
		var cfg = QueryManager.getInstance().getAttribute(entity, CFGAnalysisResult.class);
		var res = DominanceHelper.analyze(entity.getEntryBlock(), cfg.successors(), cfg.predecessors());
		return new DominanceAnalysisResult(res.dominance(), res.dominanceFrontier());
	}
}