package org.systemf.compiler.query;

import org.systemf.compiler.parser.ParserQueryRegistry;

public class QueryRegistry {
	private QueryRegistry() {}

	public static void registerAll() {
		ParserQueryRegistry.registerAll();
	}
}