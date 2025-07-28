package org.systemf.compiler.lower;

import org.systemf.compiler.lower.rv64gc.RV64GCQueryRegistry;

public class LowerQueryRegistry {
	public static void registerAll() {
		RV64GCQueryRegistry.registerAll();
	}
}
