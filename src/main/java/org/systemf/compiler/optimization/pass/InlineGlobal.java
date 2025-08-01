package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.PointerAnalysisResult;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.type.interfaces.Atom;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.query.QueryManager;

public enum InlineGlobal implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new InlineGlobalContext(module).run();
	}

	private static class InlineGlobalContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private final PointerAnalysisResult ptrResult;

		public InlineGlobalContext(Module module) {
			this.module = module;
			this.ptrResult = query.getAttribute(module, PointerAnalysisResult.class);
		}

		private boolean processGlobalVar(GlobalVariable global) {
			if (!(global.getType() instanceof Atom)) return false;
			var initializer = global.getInitializer();
			var pointers = ptrResult.pointedBy(global);
			if (pointers.stream().anyMatch(ptr -> ptrResult.pointTo(ptr).size() != 1)) return false;
			if (pointers.stream().flatMap(ptr -> ptr.getDependant().stream())
					.anyMatch(dependant -> !(dependant instanceof Load))) return false;
			var res = false;
			for (var ptr : pointers)
				for (var dep : ptr.getDependant()) {
					((Load) dep).replaceAllUsage(initializer);
					res = true;
				}
			return res;
		}

		public boolean run() {
			var res = module.getGlobalDeclarations().values().stream().map(this::processGlobalVar)
					.reduce(false, (a, b) -> a || b);
			if (res) {
				query.invalidateAllAttributes(module);
				module.getFunctions().values().forEach(query::invalidateAllAttributes);
			}
			return res;
		}
	}
}
