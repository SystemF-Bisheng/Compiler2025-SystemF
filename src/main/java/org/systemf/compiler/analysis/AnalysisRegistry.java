package org.systemf.compiler.analysis;

import org.systemf.compiler.query.QueryManager;

public class AnalysisRegistry {
	public static void registerAll() {
		var query = QueryManager.getInstance();
		query.registerProvider(CFGAnalysis.INSTANCE);
	}
}