package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.IFunction;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.query.AttributeProvider;

import java.util.HashMap;
import java.util.HashSet;

public enum CallGraphAnalysis implements AttributeProvider<Module, CallGraphAnalysisResult> {
	INSTANCE;

	private void analyzeFunction(Function function, CallGraphAnalysisResult out) {
		function.allInstructions().filter(inst -> inst instanceof AbstractCall).map(inst -> (AbstractCall) inst)
				.forEach(call -> {
					var callee = call.getFunction();
					if (!(callee instanceof IFunction calleeFunc)) return;
					out.successors(function).add(calleeFunc);
					out.predecessors(calleeFunc).add(function);
				});
	}

	@Override
	public CallGraphAnalysisResult getAttribute(Module entity) {
		var res = new CallGraphAnalysisResult(new HashMap<>(), new HashMap<>());
		for (var func : entity.getFunctions().values()) {
			res.successors().put(func, new HashSet<>());
			res.predecessors().put(func, new HashSet<>());
		}
		for (var external : entity.getExternalFunctions().values()) {
			res.successors().put(external, new HashSet<>());
			res.predecessors().put(external, new HashSet<>());
		}
		for (var func : entity.getFunctions().values()) analyzeFunction(func, res);
		return res;
	}
}
