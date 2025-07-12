import org.systemf.compiler.query.QueryRegistry;

public class Compiler {
	public static void main(String[] args) {
		CompileArgument compileArgument;

		try {
			compileArgument = parseArguments(args);
		}
		catch (IllegalArgumentException e) {
			System.err.println(e);
			System.exit(-1);
		}

		QueryRegistry.registerAll();
	}

	record CompileArgument(String inputFilePath, String outputFilePath) {}

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
				}
				else {
					/* ignore */
				}
			}
			else {
				if (inputFilePath != null) {
					throw new IllegalArgumentException("multi input files compiling is not supported so far");
				}
				inputFilePath = arg;
			}
		}

		if (inputFilePath == null) {
			throw new IllegalArgumentException("no input file is provided");
		}
		if (outputFilePath == null) {
			throw new IllegalArgumentException("no output file is provided");
		}

		return new CompileArgument(inputFilePath, outputFilePath);
	}
}
