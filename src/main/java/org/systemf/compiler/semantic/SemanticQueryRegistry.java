package org.systemf.compiler.semantic;

import org.systemf.compiler.query.QueryManager;

public class SemanticQueryRegistry {
	public static void registerAll() {
		var manager = QueryManager.getInstance();
		manager.registerProvider(SemanticChecker.INSTANCE);
		manager.registerProvider(TypeAnnotationProvider.INSTANCE);
	}
}