package org.systemf.compiler.lower.rv64gc;

import org.systemf.compiler.lower.rv64gc.allocate.RVAllocator;
import org.systemf.compiler.lower.rv64gc.analysis.RVAnalysisQueryRegistry;
import org.systemf.compiler.lower.rv64gc.lowering.RVLowering;
import org.systemf.compiler.lower.rv64gc.optimization.RVOptimizer;
import org.systemf.compiler.query.QueryManager;

public class RV64GCQueryRegistry {
	public static void registerAll() {
		var query = QueryManager.getInstance();
		query.registerProvider(RVLowering.INSTANCE);
		RVAnalysisQueryRegistry.registerAll();
		query.registerProvider(RVOptimizer.INSTANCE);
		query.registerProvider(RVAllocator.INSTANCE);
	}
}
