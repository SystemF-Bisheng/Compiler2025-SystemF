package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.CallGraphAnalysisResult;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.global.IFunction;
import org.systemf.compiler.query.QueryManager;

import java.util.HashSet;

public enum RemoveUnusedFunction implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new RemoveUnusedFunctionContext(module).run();
	}

	private static class RemoveUnusedFunctionContext {
		private final Module module;
		private final QueryManager query = QueryManager.getInstance();
		private final CallGraphAnalysisResult callGraph;
		private final HashSet<IFunction> reachable = new HashSet<>();

		public RemoveUnusedFunctionContext(Module module) {
			this.module = module;
			this.callGraph = query.getAttribute(module, CallGraphAnalysisResult.class);
		}

		private void markReachable(IFunction cur) {
			if (reachable.contains(cur)) return;
			reachable.add(cur);
			callGraph.successors(cur).forEach(this::markReachable);
		}

		public boolean run() {
			var res = false;

			var main = module.getFunction("main");
			markReachable(main);

			var unreachableFunc = module.getFunctions().values().stream().filter(func -> !reachable.contains(func))
					.toList();
			if (!unreachableFunc.isEmpty()) res = true;
			unreachableFunc.forEach(func -> {
				query.invalidateAllAttributes(func);
				module.removeFunction(func);
			});

			var unreachableExternal = module.getExternalFunctions().values().stream()
					.filter(func -> !reachable.contains(func)).toList();
			if (!unreachableExternal.isEmpty()) res = true;
			unreachableExternal.forEach(func -> {
				query.invalidateAllAttributes(func);
				module.removeExternalFunction(func);
			});

			if (res) query.invalidateAllAttributes(module);
			return res;
		}
	}
}
