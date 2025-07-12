package org.systemf.compiler.query;

import org.systemf.compiler.machine.MachineQueryRegistry;
import org.systemf.compiler.parser.ParserQueryRegistry;

public class QueryRegistry {
	private QueryRegistry() {}

	public static void registerAll() {
		ParserQueryRegistry.registerAll();
		MachineQueryRegistry.registerAll();
	}
}