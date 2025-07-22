package org.systemf.compiler.semantic;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.query.QueryRegistry;

public class SemanticTest {
	public static void main(String[] args) {
		QueryRegistry.registerAll();
		var query = QueryManager.getInstance();
		var code = CharStreams.fromString("""
				int a = 0;
				int main() {
					int b = 1;
					int c = a + b;
					c = c + 1;
					int d = 1.0;
					int arr[1];
					arr[arr] = 0;
					return c;
				}
				""");
		query.registerProvider(CharStream.class, () -> code);
		query.get(SemanticResult.class);
	}
}