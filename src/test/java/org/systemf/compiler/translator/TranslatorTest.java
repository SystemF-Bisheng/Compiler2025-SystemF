package org.systemf.compiler.translator;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.query.QueryRegistry;

public class TranslatorTest {
	public static void main(String[] args) {
		QueryRegistry.registerAll();
		var query = QueryManager.getInstance();
		var code = CharStreams.fromString("""
				int a = 0;
				int global_arr[3][3] = {1, {2, 3}, 4, 5};
				int main(int arg, int arr_in[][3]) {
					arg = arg + 1;
					arr_in[0][1] = arg;
					global_arr[0][0] = arg;
					int b = 1;
					int c = a + b;
					c = c + 1;
					int d = 1.0;
					int arr[2][3] = {1, 2, 3, 4, 5, d};
					arr[0][1] = 0;
					if (b && c) {
						d = 2.0;
					}
					while (b > 0) {
						b = b - 1;
						if (c < 5) {
							break;
						}
					}
					if (b == 0) return c;
				}
				""");
		query.registerProvider(CharStream.class, () -> code);
		query.get(IRTranslatedResult.class).module().dump(System.out);
	}
}