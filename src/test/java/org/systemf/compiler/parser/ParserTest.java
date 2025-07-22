package org.systemf.compiler.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.query.QueryRegistry;

public class ParserTest {
	public static void main(String[] args) {
		QueryRegistry.registerAll();
		var query = QueryManager.getInstance();
		var code = CharStreams.fromString("""
				int a = 0;
				int main() {
					int b = 1;
					int c = a + b;
					c = c + 1;
					float x = 2.0;
					return c;
				}
				""");
		query.registerProvider(CharStream.class, () -> code);
		var parsed = query.get(ParsedResult.class);
		var formatted = query.getAttribute(parsed.program(), PrettyPrintAttribute.class);
		System.out.println(formatted.result());
	}
}