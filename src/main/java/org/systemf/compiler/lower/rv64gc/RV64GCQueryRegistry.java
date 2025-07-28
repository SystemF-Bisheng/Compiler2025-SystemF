package org.systemf.compiler.lower.rv64gc;

import org.systemf.compiler.lower.rv64gc.lowering.RVLowering;
import org.systemf.compiler.query.QueryManager;

public class RV64GCQueryRegistry {
	public static void registerAll() {
		QueryManager.getInstance().registerProvider(RVLowering.INSTANCE);
	}
}
