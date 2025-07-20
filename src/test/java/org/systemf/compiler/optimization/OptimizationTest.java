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
				int test() {
					int x = getint();
					int b;
					int tmp = getint();
					if (x > 3) {
						b = tmp;
					} else {
						b = tmp;
					}
					return b;
				}
				int main()
				{
					int cnt = getint();
					int x = 5;
					int y = 3;
					while (cnt) {
						int tmp = x;
						x = y;
						y = tmp;
						cnt = cnt - 1;
					}
					int a, b;
					if (x == 1) {
						a = 5;
					} else {
						a = 5;
					}
					while (x) {
						b = a + a + a;
						x = x - 1;
					}
					b = a + a;
					b = b + test();
					return b;
				}
				
				""");
		query.registerProvider(CharStream.class, () -> code);
		query.get(OptimizedResult.class).module().dump(System.out);
	}
}