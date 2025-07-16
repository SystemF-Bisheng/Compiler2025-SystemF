package org.systemf.compiler.interpreter;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.query.QueryRegistry;
import org.systemf.compiler.translator.IRTranslatedResult;

import java.util.Scanner;

public class IRInterpreterTest {
	public static void main(String[] args) {

		QueryRegistry.registerAll();
		var query = QueryManager.getInstance();

		var input = CharStreams.fromString("""
				// float global constants
				const float RADIUS = 5.5, PI = 03.141592653589793, EPS = 1e-6;
				
				// hexadecimal float constant
				const float PI_HEX = 0x1.921fb6p+1;
				
				// float constant evaluation
				const float EVAL2 = 2 * PI_HEX * RADIUS, EVAL3 = PI * 2 * RADIUS;
				
				// float -> float function
				float float_abs(float x) {
				  if (x < 0) return -x;
				  return x;
				}
				
				// float -> float -> int function & float/int expression
				int float_eq(float a, float b) {
				  float dis = float_abs(a - b);
				  putfloat(dis);
				  if (dis < EPS) {
				    return 1 * 2. / 2;
				  } else {
				    return 0;
				  }
				}
				
				void error() {
				  putch(101);
				  putch(114);
				  putch(114);
				  putch(111);
				  putch(114);
				  putch(10);
				}
				
				void ok() {
				  putch(111);
				  putch(107);
				  putch(10);
				}
				
				void assert(int cond) {
				  if (!cond) {
				    error();
				  } else {
				    ok();
				  }
				}
				
				int main() {
				  assert(float_eq(EVAL2, EVAL3));
				  return 0;
				}
				""");
		query.registerProvider(CharStream.class, () -> input);
		Module module = query.get(IRTranslatedResult.class).module();

		IRInterpreter irInterpreter = new IRInterpreter();
		Scanner scanner = new Scanner(System.in);
		irInterpreter.execute(module, scanner, System.out);
		scanner.close();
		System.exit(irInterpreter.getMainRet());
	}
}