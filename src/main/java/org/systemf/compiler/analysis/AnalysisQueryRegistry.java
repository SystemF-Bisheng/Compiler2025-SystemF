package org.systemf.compiler.analysis;

import org.systemf.compiler.query.QueryManager;

public class AnalysisQueryRegistry {
	public static void registerAll() {
		var query = QueryManager.getInstance();
		query.registerProvider(CFGAnalysis.INSTANCE);
		query.registerProvider(PointerAnalysis.INSTANCE);
		query.registerProvider(DominanceAnalysis.INSTANCE);
	}
}