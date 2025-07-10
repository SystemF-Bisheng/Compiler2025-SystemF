package org.systemf.compiler.query;

import org.systemf.compiler.parser.ParsedProvider;

public class QueryRegistry {
	public static void registerAll() {
		QueryManager.getInstance().registerProvider(ParsedProvider.INSTANCE);
	}
}