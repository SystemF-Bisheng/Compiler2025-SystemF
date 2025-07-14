package org.systemf.compiler.semantic;

import org.antlr.v4.runtime.ParserRuleContext;

public class IllegalSemanticException extends RuntimeException {
	public IllegalSemanticException(String message) {
		super(message);
	}

	public IllegalSemanticException(String message, Throwable cause) {
		super(message, cause);
	}

	public static IllegalSemanticException wrap(ParserRuleContext context, Throwable cause) {
		var start = context.getStart();
		var stop = context.getStop();
		var newException = new IllegalSemanticException(
				String.format("Semantic error in context from %d:%d to %d:%d", start.getLine(),
						start.getCharPositionInLine(), stop.getLine(), stop.getCharPositionInLine()), cause);
		newException.setStackTrace(new StackTraceElement[0]);
		return newException;
	}
}