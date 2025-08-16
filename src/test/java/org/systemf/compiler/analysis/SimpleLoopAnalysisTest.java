package org.systemf.compiler.analysis;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.systemf.compiler.optimization.OptimizedResult;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.query.QueryRegistry;

public class SimpleLoopAnalysisTest {
	public static void main(String[] args) {
		QueryRegistry.registerAll();
		var query = QueryManager.getInstance();
		var code = CharStreams.fromString("""
				void spmv(int n,int xptr[], int yidx[], int vals[], int b[], int x[]){
				      int i, j, k;
				      i = 0;
				      while (i < n){
				          x[i] = 0;
				          i = i + 1;
				      }
				
				      i = 0;
				      while (i < n){
				          j = xptr[i];
				          while (j < xptr[i + 1]){
				              x[yidx[j]] = x[yidx[j]] + vals[j];
				              j = j + 1;
				          }
				
				          j = xptr[i];
				          while (j < xptr[i + 1]){
				              x[yidx[j]] = x[yidx[j]] + vals[j] * (b[i] - 1);
				              j = j + 1;
				          }
				          i = i + 1;
				      }
				  }
				
				  const int N = 100010;
				  const int M = 3000000;
				
				  int x[N], y[M], v[M];
				  int a[N], b[N], c[N];
				
				  int main(){
				      int n = getarray(x) - 1;
				      int m = getarray(y);
				      getarray(v);
				
				      getarray(a);
				
				      starttime();
				
				      int i = 0;
				      while (i < 5){
				          spmv(n, x, y, v, a, b);
				          spmv(n, x, y, v, b, a);
				          i=i+1;
				      }
				      stoptime();
				      putarray(n, b);
				      return 0;
				  }
				
				""");
		query.registerProvider(CharStream.class, () -> code);
		var module = query.get(OptimizedResult.class).module();
		module.getFunctions().values().forEach(func -> {
			System.out.println(func.getName());
			var result = query.getAttribute(func, SimpleLoopAnalysisResult.class);
			for (var loop : result.loops())
				System.out.printf("%s, %s, %s\n",
						loop.head().getName(),
						loop.body().getName(),
						loop.merge().getName());
		});
	}
}
