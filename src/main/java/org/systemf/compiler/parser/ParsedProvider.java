package org.systemf.compiler.parser;

import org.antlr.v4.runtime.*;
import org.systemf.compiler.query.EntityProvider;
import org.systemf.compiler.query.QueryManager;

public enum ParsedProvider implements EntityProvider<ParsedResult> {
	INSTANCE;

	@Override
	public ParsedResult produce() {
		var stream = QueryManager.getInstance().get(CharStream.class);
		var exception = new IllegalSyntaxException[1];
		var lexer = new SysYLexer(stream);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new ParsingErrorListener("Lexer", exception));

		var parser = new SysYParser(new BufferedTokenStream(lexer));
		parser.removeErrorListeners();
		parser.addErrorListener(new ParsingErrorListener("Parser", exception));

		var program = parser.program();
		if (exception[0] != null) throw exception[0];

		return new ParsedResult(program);
	}

	private static class ParsingErrorListener extends BaseErrorListener {
		private final String type;
		private final IllegalSyntaxException[] exception;

		public ParsingErrorListener(String type, IllegalSyntaxException[] exception) {
			this.type = type;
			this.exception = exception;
		}

		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
				String msg, RecognitionException e) {
			var cur = new IllegalSyntaxException(
					String.format("%s error at Line %d: %s at char %d.", type, line, msg, charPositionInLine));
			if (exception[0] != null) exception[0].addSuppressed(cur);
			else exception[0] = cur;
		}
	}
}