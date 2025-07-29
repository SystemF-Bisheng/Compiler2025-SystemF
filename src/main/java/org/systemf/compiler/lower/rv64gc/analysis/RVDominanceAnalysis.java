package org.systemf.compiler.lower.rv64gc.analysis;

import org.systemf.compiler.analysis.util.DominanceHelper;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.query.AttributeProvider;
import org.systemf.compiler.query.QueryManager;

/**
 * Depend on: CFGAnalysis
 * <p>
 * Applicable to: IR
 */
public enum RVDominanceAnalysis implements AttributeProvider<Function, RVDominanceAnalysisResult> {
	INSTANCE;

	@Override
	public RVDominanceAnalysisResult getAttribute(Function entity) {
		var cfg = QueryManager.getInstance().getAttribute(entity, RVCFGAnalysisResult.class);
		var res = DominanceHelper.analyze(entity.getEntryBlock(), cfg.successors(), cfg.predecessors());
		return new RVDominanceAnalysisResult(res.dominance(), res.dominanceFrontier());
	}
}