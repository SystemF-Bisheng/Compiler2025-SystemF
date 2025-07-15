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
				int buf[2][100];
				
				// sort [l, r)
				void merge_sort(int l, int r)
				{
				    if (l + 1 >= r)
				        return;
				
				    int mid = (l + r) / 2;
				    merge_sort(l, mid);
				    merge_sort(mid, r);
				
				    int i = l, j = mid, k = l;
				    while (i < mid && j < r) {
				        if (buf[0][i] < buf[0][j]) {
				            buf[1][k] = buf[0][i];
				            i = i + 1;
				        } else {
				            buf[1][k] = buf[0][j];
				            j = j + 1;
				        }
				        k = k + 1;
				    }
				    while (i < mid) {
				        buf[1][k] = buf[0][i];
				        i = i + 1;
				        k = k + 1;
				    }
				    while (j < r) {
				        buf[1][k] = buf[0][j];
				        j = j + 1;
				        k = k + 1;
				    }
				
				    while (l < r) {
				        buf[0][l] = buf[1][l];
				        l = l + 1;
				    }
				}
				
				int main()
				{
				    int n = getarray(buf[0]);
				    merge_sort(0, n);
				    putarray(n, buf[0]);
				    return 0;
				}
				
				""");
		query.registerProvider(CharStream.class, () -> code);
		query.get(IRTranslatedResult.class).module().dump(System.out);
	}
}