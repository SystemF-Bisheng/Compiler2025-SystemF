package org.systemf.compiler.analysis;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.optimization.OptimizedResult;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.query.QueryRegistry;

import java.util.stream.Collectors;

public class IndexVarianceAnalysisTest {
	public static void main(String[] args) {
		QueryRegistry.registerAll();
		var query = QueryManager.getInstance();
		var code = CharStreams.fromString("""
				const int N = 1024;
				
				
				void trsm(int n, float A[][N], float B[][N]) {
				  int i, j, k;
				
				  k = 0;
				  while (k < n) {
				    i = 0;
				    while (i < n) {
				      B[i][k] = B[i][k] / A[i][i];
				      j = i + 1;
				      while (j < n) {
				        B[j][k] = B[j][k] - A[j][i] * B[i][k];
				        j = j + 1;
				      }
				      i = i + 1;
				    }
				    k = k + 1;
				  }
				}
				
				float A[N][N];
				float B[N][N];
				float C[N][N];
				
				int main() {
				  int n = getint();
				  int i, j, k;
				
				  i = 0;
				  j = 0;
				  while (i < n) {
				    j = 0;
				    while (j < n) {
				      A[i][j] = getfloat();
				      j = j + 1;
				    }
				    i = i + 1;
				  }
				  i = 0;
				  j = 0;
				  while (i < n) {
				    j = 0;
				    while (j < n) {
				      C[i][j] = getfloat();
				      j = j + 1;
				    }
				    i = i + 1;
				  }
				
				 starttime();
				  k = 0;
				  while (k < 5) {
				    i = 0;
				    while (i < n) {
				      j = 0;
				      while (j < n) {
				        B[i][j] = C[i][j];
				        j = j + 1;
				      }
				      i = i + 1;
				    }
				    trsm(n, A, B);
				    k = k + 1;
				  }
				  stoptime();
				
				  float ans = 0;
				  i = 0;
				  while (i < n) {
				    j = 0;
				    while (j < n) {
				      ans = ans + B[i][j];
				      j = j + 1;
				    }
				    i = i + 1;
				  }
				
				  putfloat(ans);
				  putch(10);
				
				  return 0;
				}
				
				""");
		query.registerProvider(CharStream.class, () -> code);
		var module = query.get(OptimizedResult.class).module();
		var result = query.getAttribute(module, IndexVarianceAnalysisResult.class);
		result.variance().forEach((alloc, variance) -> System.out.printf("%s: %s\n", ValueUtil.dumpIdentifier(alloc),
				variance.stream().map(String::valueOf).collect(Collectors.joining(", "))));
	}
}
