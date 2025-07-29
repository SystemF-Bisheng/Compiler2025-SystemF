package org.systemf.compiler.lower.rv64gc.analysis;

import org.systemf.compiler.query.QueryManager;

public class RVAnalysisQueryRegistry {
	public static void registerAll() {
		var query = QueryManager.getInstance();
		query.registerProvider(RVCFGAnalysis.INSTANCE);
		query.registerProvider(RVDominanceAnalysis.INSTANCE);
	}
}
