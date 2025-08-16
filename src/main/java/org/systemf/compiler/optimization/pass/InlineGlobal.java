package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.DominanceAnalysisResult;
import org.systemf.compiler.analysis.PointerAnalysisResult;
import org.systemf.compiler.analysis.util.BelongingHelper;
import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.type.interfaces.Atom;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.query.QueryManager;

import java.util.ArrayList;
import java.util.Map;

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
		private final Map<Instruction, Function> belonging;
		private IRBuilder builder;

		public InlineGlobalContext(Module module) {
			this.module = module;
			this.ptrResult = query.getAttribute(module, PointerAnalysisResult.class);
			this.belonging = BelongingHelper.getBelonging(module);
		}

		private boolean processGlobalVar(GlobalVariable global) {
			if (!(global.getType() instanceof Atom)) return false;
			var initializer = global.getInitializer();
			var pointers = ptrResult.pointedBy(global);
			if (pointers.stream().anyMatch(ptr -> ptrResult.pointTo(ptr).size() != 1)) return false;

			if (pointers.stream().flatMap(ptr -> ptr.getDependant().stream())
					.allMatch(dependant -> dependant instanceof Load)) { // Global constant
				var res = false;
				for (var ptr : pointers)
					for (var dep : ptr.getDependant()) {
						((Load) dep).replaceAllUsage(initializer);
						res = true;
					}
				return res;
			} else if (pointers.size() == 1 && pointers.contains(global)) { // Actual local
				Function func = null;
				for (var inst : global.getDependant()) {
					var belong = belonging.get(inst);
					if (func == null) func = belong;
					else if (func != belong) return false;
				}
				if (func == null) return false;

				if (func !=
				    module.getMainFunction()) { // Need no further check, if it's main function (called only once)
					// Check whether this global is always overwritten
					var positions = BelongingHelper.getPositions(func);
					var domTree = query.getAttribute(func, DominanceAnalysisResult.class).dominance();
					var dependants = new ArrayList<>(global.getDependant());
					dependants.sort((a, b) -> positions.get(a).compare(positions.get(b), domTree));
					if (!(dependants.getFirst() instanceof Store firstStore)) return false;
					var firstPos = positions.get(firstStore);
					if (!dependants.stream().map(positions::get)
							.allMatch(dependant -> firstPos.dominate(dependant, domTree)))
						return false;
				}

				builder.attachToBlockHead(func.getEntryBlock());
				var newAlloc = builder.buildAlloca(global.valueType, global.getName());
				builder.buildStore(global.getInitializer(), newAlloc);
				global.replaceAllUsage(newAlloc);
				return true;
			}
			return false;
		}

		public boolean run() {
			try (var builder = new IRBuilder(module)) {
				this.builder = builder;
				var res = false;
				for (var global : module.getGlobalDeclarations().values())
					if (processGlobalVar(global)) {
						res = true;
						break;
					}
				if (res) {
					query.invalidateAllAttributes(module);
					module.getFunctions().values().forEach(query::invalidateAllAttributes);
				}
				return res;
			}
		}
	}
}
