package org.systemf.compiler.translator;

import org.systemf.compiler.query.EntityProvider;

public enum IRTranslator implements EntityProvider<IRTranslatedResult> {
	INSTANCE;

	@Override
	public IRTranslatedResult produce() {
		// TODO: Implement translator to IR
		throw new UnsupportedOperationException();
	}
}