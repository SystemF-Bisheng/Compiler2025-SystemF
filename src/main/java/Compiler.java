import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.systemf.compiler.interpreter.IRInterpreter;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.query.QueryRegistry;
import org.systemf.compiler.translator.IRTranslatedResult;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Compiler {
	public static void main(String[] args) throws IOException {
		CompileArgument compileArgument = parseArguments(args);

		QueryRegistry.registerAll();
		var query = QueryManager.getInstance();

		if (compileArgument.simulateMode()){
			IRInterpreter irInterpreter = new IRInterpreter();
			String simulateInput = "";
			CharStream compileInput;
			if (!compileArgument.inputFilePath().getFirst().equals(".in")) {
				compileInput = CharStreams.fromFileName(compileArgument.inputFilePath().getFirst());
				simulateInput = Files.readString(Path.of(compileArgument.inputFilePath().get(1)));
			} else {compileInput = null;}
			query.registerProvider(CharStream.class, () -> compileInput);
			Module module = query.get(IRTranslatedResult.class).module();
			irInterpreter.execute(module, simulateInput);
			irInterpreter.dump(new PrintStream(compileArgument.outputFilePath()));
			return;
		}

		var input = CharStreams.fromFileName(compileArgument.inputFilePath().getFirst());
		query.registerProvider(CharStream.class, () -> input);
		Module module = query.get(IRTranslatedResult.class).module();
		module.dump(new PrintStream(compileArgument.outputFilePath()));

	}

	static CompileArgument parseArguments(String[] args) throws IllegalArgumentException {
		String  outputFilePath = null;
		List<String> inputPaths = new ArrayList<>();
		boolean simulateMode = false;

		for (int i = 0; i < args.length; i++) {
			var arg = args[i];
			if (arg.charAt(0) == '-') {
				if (arg.equals("-o")) {
					if (i + 1 >= args.length) {
						throw new IllegalArgumentException("`-o` requires an argument");
					}
					outputFilePath = args[i + 1];
					i++;
				} else if (arg.equals("--simulated")) {
					simulateMode = true;
				} else {
					/* ignore */
				}
			} else {
				inputPaths.add(arg);
			}
		}

		if (inputPaths.isEmpty()) {
			throw new IllegalArgumentException("no input file is provided");
		}
		if (outputFilePath == null) {
			throw new IllegalArgumentException("no output file is provided");
		}
		if (!simulateMode && inputPaths.size() > 1 || simulateMode && inputPaths.size() > 2) {
			throw new IllegalArgumentException("multiple input files are not allowed in non-simulated mode");
		}

		return new CompileArgument(inputPaths, outputFilePath, simulateMode);
	}

	record CompileArgument(List<String> inputFilePath, String outputFilePath, boolean simulateMode) {
	}
}