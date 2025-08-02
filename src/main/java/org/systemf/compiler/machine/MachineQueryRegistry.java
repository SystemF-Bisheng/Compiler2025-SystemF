package org.systemf.compiler.machine;

import org.systemf.compiler.machine.rv64gc.RVGenerator;
import org.systemf.compiler.query.QueryManager;

public class MachineQueryRegistry {
	public static void registerAll() {
		QueryManager.getInstance().registerProvider(RVGenerator.INSTANCE);
	}
}