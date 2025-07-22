package org.systemf.compiler.optimization;

import org.systemf.compiler.query.QueryManager;

public class OptimizationQueryRegistry {
	public static void registerAll() {
		QueryManager.getInstance().registerProvider(Optimizer.INSTANCE);
	}
}