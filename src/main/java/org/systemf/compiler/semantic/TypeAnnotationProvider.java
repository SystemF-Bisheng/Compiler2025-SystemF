package org.systemf.compiler.semantic;

import org.antlr.v4.runtime.ParserRuleContext;
import org.systemf.compiler.query.AttributeProvider;
import org.systemf.compiler.query.QueryManager;

import java.util.NoSuchElementException;

public enum TypeAnnotationProvider implements AttributeProvider<ParserRuleContext, ValueAndType> {
	INSTANCE;

	@Override
	public ValueAndType getAttribute(ParserRuleContext entity) {
		var result = QueryManager.getInstance().get(SemanticResult.class).typeMap().get(entity);
		if (result == null) throw new NoSuchElementException("The given context has no type annotation");
		return result;
	}
}