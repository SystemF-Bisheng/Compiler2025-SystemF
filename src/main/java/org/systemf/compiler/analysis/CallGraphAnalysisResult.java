package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.global.IFunction;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public record CallGraphAnalysisResult(Map<IFunction, Set<IFunction>> successors,
                                      Map<IFunction, Set<IFunction>> predecessors) {
	public Set<IFunction> successors(IFunction function) {
		var res = successors.get(function);
		if (res == null) throw new NoSuchElementException("No such function: " + function.getName());
		return res;
	}

	public Set<IFunction> predecessors(IFunction function) {
		var res = predecessors.get(function);
		if (res == null) throw new NoSuchElementException("No such function: " + function.getName());
		return res;
	}
}
