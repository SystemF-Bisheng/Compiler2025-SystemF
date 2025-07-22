package org.systemf.compiler.analysis;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.query.QueryRegistry;
import org.systemf.compiler.translator.IRTranslatedResult;

public class DominanceAnalysisTest {
	public static void main(String[] args) {
		QueryRegistry.registerAll();
		var query = QueryManager.getInstance();
		var code = CharStreams.fromString("""
				int main() {
					int a = 0;
					if (a < 1) {
						while (a>3) {
							if (a > 5) break;
						}
						a = 3;
						while (a<3) {
							if (a > 8) break;
						}
						a = 5;
					} else {
						while (a>3) {
							a = 3;
						}
					}
				
					if (a == 3) {
						a = 2;
					}
				    return 0;
				}
				
				""");
		query.registerProvider(CharStream.class, () -> code);
		var module = query.get(IRTranslatedResult.class).module();
		var mainFunc = module.getFunction("main");
		var result = query.getAttribute(mainFunc, DominanceAnalysisResult.class).dominance();
		System.out.println(mainFunc);
		for (var block : mainFunc.getBlocks()) {
			var iDom = result.getParent(block);
			System.out.printf("%s: %s\n", block.getName(), iDom == null ? "null" : iDom.getName());
		}
	}
}