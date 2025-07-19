package org.systemf.compiler.optimization;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.query.QueryRegistry;

public class OptimizationTest {
	public static void main(String[] args) {
		QueryRegistry.registerAll();
		var query = QueryManager.getInstance();
		var code = CharStreams.fromString("""
				int main()
				{
				    if (0) {
				        return 1;
				    }
				    return 0;
				}
				
				""");
		query.registerProvider(CharStream.class, () -> code);
		query.get(OptimizedResult.class).module().dump(System.out);
	}
}