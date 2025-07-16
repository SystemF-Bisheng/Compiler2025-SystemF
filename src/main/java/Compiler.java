import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.systemf.compiler.interpreter.IRInterpreter;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.query.QueryRegistry;
import org.systemf.compiler.translator.IRTranslatedResult;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

public class Compiler {
	public static void main(String[] args) throws IOException {
		CompileArgument compileArgument = parseArguments(args);

		QueryRegistry.registerAll();
		var query = QueryManager.getInstance();

		var input = CharStreams.fromFileName(compileArgument.inputFilePath());
		query.registerProvider(CharStream.class, () -> input);
		Module module = query.get(IRTranslatedResult.class).module();

		if (compileArgument.outputFilePath() != null) {
			module.dump(new PrintStream(compileArgument.outputFilePath()));
		} else {
			IRInterpreter irInterpreter = new IRInterpreter();
			Scanner scanner = new Scanner(System.in);
			irInterpreter.execute(module, scanner, System.out);
			scanner.close();
			System.exit(irInterpreter.getMainRet());
		}
	}

	static CompileArgument parseArguments(String[] args) throws IllegalArgumentException {
		String inputFilePath = null, outputFilePath = null;

		for (int i = 0; i < args.length; i++) {
			var arg = args[i];
			if (arg.charAt(0) == '-') {
				if (arg.equals("-o")) {
					if (i + 1 >= args.length) {
						throw new IllegalArgumentException("`-o` requires an argument");
					}
					outputFilePath = args[i + 1];
					i++;
				} else {
					/* ignore */
				}
			} else {
				inputFilePath = arg;
			}
		}

		if (inputFilePath == null) {
			throw new IllegalArgumentException("no input file is provided");
		}

		return new CompileArgument(inputFilePath, outputFilePath);
	}

	record CompileArgument(String inputFilePath, String outputFilePath) {
	}
}