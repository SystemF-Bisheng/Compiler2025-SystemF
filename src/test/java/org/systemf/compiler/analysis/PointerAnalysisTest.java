package org.systemf.compiler.analysis;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.query.QueryRegistry;
import org.systemf.compiler.translator.IRTranslatedResult;

import java.util.stream.Collectors;

public class PointerAnalysisTest {
	public static void main(String[] args) {
		QueryRegistry.registerAll();
		var query = QueryManager.getInstance();
		var code = CharStreams.fromString("""
				int buf[2][100];
				int buf2[100];
				
				int func1(int arr[]) {
				    return arr[0];
				}
				
				int func2() {
					return func1(buf2);
				}
				
				int main() {
					int local[2][100];
				    func1(buf[1]);
				    func1(local);
				    func2();
				    return 0;
				}
				
				""");
		query.registerProvider(CharStream.class, () -> code);
		var module = query.get(IRTranslatedResult.class).module();
		var result = query.getAttribute(module, PointerAnalysisResult.class);
		result.pointTo().forEach((value, pointTo) -> {
			System.out.printf("%s: ", ValueUtil.dumpIdentifier(value));
			System.out.println(pointTo.stream().map(ValueUtil::dumpIdentifier).collect(Collectors.joining(", ")));
		});
	}
}