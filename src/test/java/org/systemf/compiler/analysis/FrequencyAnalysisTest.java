package org.systemf.compiler.analysis;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.systemf.compiler.optimization.OptimizedResult;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.query.QueryRegistry;

public class FrequencyAnalysisTest {
	public static void main(String[] args) {
		QueryRegistry.registerAll();
		var query = QueryManager.getInstance();
		var code = CharStreams.fromString("""
				const int N = 1024;
				
				void mm(int n, int A[][N], int B[][N], int C[][N]){
				    int i, j, k;
				
				    if (n > 5) n = n - 1;
				
				    i = 0; j = 0;
				    while (i < n){
				        j = 0;
				        while (1) {
				            j = j + 3;
				            if (j > 15) break;
				            while (j < n) {
				                j = j + 1;
				                if (j > 100) break;
				            }
				            if (j + j > 30) break;
				            j = j + 5;
				            if (j > 15) break;
				            if (j + j > 30) break;
				        }
				        j = j + 3;
				
				        while (j < n){
				            if (j > 5) j = j + 2;
				            if (j > 8) j = j + 5;
				            C[i][j] = 0;
				            j = j + 1;
				        }
				        i = i + 1;
				    }
				
				    i = 0; j = 0; k = 0;
				
				    while (k < n){
				        i = 0;
				        while (i < n){
				            if (A[i][k] == 0){
				                i = i + 1;
				                continue;
				            }
				            j = 0;
				            while (j < n){
				                C[i][j] = C[i][j] + A[i][k] * B[k][j];
				                j = j + 1;
				            }
				            i = i + 1;
				        }
				        k = k + 1;
				    }
				}
				
				int A[N][N];
				int B[N][N];
				int C[N][N];
				
				int main(){
				    int n = getint();
				    int i, j;
				
				    i = 0;
				    j = 0;
				    while (i < n){
				        j = 0;
				        while (j < n){
				            A[i][j] = getint();
				            j = j + 1;
				        }
				        i = i + 1;
				    }
				    i = 0;
				    j = 0;
				    while (i < n){
				        j = 0;
				        while (j < n){
				            B[i][j] = getint();
				            j = j + 1;
				        }
				        i = i + 1;
				    }
				
				    starttime();
				
				    i = 0;
				    while (i < 5){
				        mm(n, A, B, C);
				        mm(n, A, C, B);
				        i = i + 1;
				    }
				
				    int ans = 0;
				    i = 0;
				    while (i < n){
				        j = 0;
				        while (j < n){
				            ans = ans + B[i][j];
				            j = j + 1;
				        }
				        i = i + 1;
				    }
				    stoptime();
				    putint(ans);
				    putch(10);
				
				    return 0;
				}
				
				""");
		query.registerProvider(CharStream.class, () -> code);
		var module = query.get(OptimizedResult.class).module();
		var mainFunc = module.getFunction("mm");
		var result = query.getAttribute(mainFunc, FrequencyAnalysisResult.class);
		for (var block : mainFunc.getBlocks()) {
			System.out.println(result.frequency(block));
			System.out.println(block);
		}
	}
}
