package org.systemf.compiler.query;

import org.systemf.compiler.analysis.AnalysisQueryRegistry;
import org.systemf.compiler.machine.MachineQueryRegistry;
import org.systemf.compiler.optimization.OptimizationQueryRegistry;
import org.systemf.compiler.parser.ParserQueryRegistry;
import org.systemf.compiler.semantic.SemanticQueryRegistry;
import org.systemf.compiler.translator.TranslatorQueryRegistry;

public class QueryRegistry {
	private QueryRegistry() {}

	public static void registerAll() {
		ParserQueryRegistry.registerAll();
		SemanticQueryRegistry.registerAll();
		TranslatorQueryRegistry.registerAll();
		AnalysisQueryRegistry.registerAll();
		OptimizationQueryRegistry.registerAll();
		MachineQueryRegistry.registerAll();
	}
}