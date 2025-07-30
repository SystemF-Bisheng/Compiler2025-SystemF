package org.systemf.compiler.lower.rv64gc;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.systemf.compiler.lower.rv64gc.allocate.RVAllocatedResult;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.query.QueryRegistry;

public class RVAllocationTest {
	public static void main(String[] args) {
		QueryRegistry.registerAll();
		var query = QueryManager.getInstance();
		var code = CharStreams.fromString("""
				const int N = 1024;
				
				void mm(int n, int A[][N], int B[][N], int C[][N]){
				    int i, j, k;
				
				    i = 0; j = 0;
				    while (i < n){
				        j = 0;
				        while (j < n){
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
					int arr1[5];
					getarray(arr1);
					int arr2[2];
					getarray(arr2);
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
		query.get(RVAllocatedResult.class).module().module().dump(System.out);
	}
}
