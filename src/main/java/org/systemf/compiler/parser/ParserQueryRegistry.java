package org.systemf.compiler.parser;

import org.systemf.compiler.query.QueryManager;

public class ParserQueryRegistry {
	private ParserQueryRegistry() {}

	public static void registerAll() {
		var manager = QueryManager.getInstance();
		manager.registerProvider(ProgramParser.INSTANCE);
		manager.registerProvider(PrettyPrinter.INSTANCE);
	}
}