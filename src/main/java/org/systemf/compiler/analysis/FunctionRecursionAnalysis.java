package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.IFunction;
import org.systemf.compiler.query.AttributeProvider;
import org.systemf.compiler.query.QueryManager;

import java.util.ArrayDeque;
import java.util.HashSet;

public enum FunctionRecursionAnalysis implements AttributeProvider<Module, FunctionRecursionAnalysisResult> {
	INSTANCE;

	private boolean checkRecursive(CallGraphAnalysisResult callGraph, Function function) {
		var visited = new HashSet<IFunction>();
		var worklist = new ArrayDeque<>(callGraph.successors(function));
		while (!worklist.isEmpty()) {
			var current = worklist.poll();
			if (!visited.add(current)) continue;
			if (current == function) return true;
			worklist.addAll(callGraph.successors(current));
		}
		return false;
	}

	@Override
	public FunctionRecursionAnalysisResult getAttribute(Module entity) {
		var query = QueryManager.getInstance();
		var callGraph = query.getAttribute(entity, CallGraphAnalysisResult.class);
		var res = new HashSet<Function>();
		entity.getFunctions().values().stream().filter(func -> checkRecursive(callGraph, func)).forEach(res::add);
		return new FunctionRecursionAnalysisResult(res);
	}
}
