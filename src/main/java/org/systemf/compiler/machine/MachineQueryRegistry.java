package org.systemf.compiler.machine;

import org.systemf.compiler.query.QueryManager;

public class MachineQueryRegistry {
	public static void registerAll() {
		QueryManager.getInstance().registerProvider(RISCVGenerator.INSTANCE);
	}
}