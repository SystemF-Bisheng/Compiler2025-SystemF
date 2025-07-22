package org.systemf.compiler.translator;

import org.systemf.compiler.query.QueryManager;

public class TranslatorQueryRegistry {
	public static void registerAll() {
		QueryManager.getInstance().registerProvider(IRTranslator.INSTANCE);
	}
}