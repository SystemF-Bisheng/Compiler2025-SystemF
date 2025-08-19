package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.PointerAnalysisResult;
import org.systemf.compiler.ir.AllocationSite;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Alloca;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.optimization.pass.util.ChainedRemoveHelper;
import org.systemf.compiler.query.QueryManager;

import java.util.HashSet;

/**
 * Remove allocation sites that are neither loaded nor used in instructions with side effect other than Store
 * <p>
 * Depend on: PointerAnalysis, FunctionSideEffectAnalysis
 * <p>
 * Applicable to: IR
 */
public enum RemoveUnusedAllocation implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new RemoveUnusedAllocationContext(module).run();
	}

	private static class RemoveUnusedAllocationContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private final PointerAnalysisResult ptrResult;

		public RemoveUnusedAllocationContext(Module module) {
			this.module = module;
			this.ptrResult = query.getAttribute(module, PointerAnalysisResult.class);
		}

		private boolean actuallyUsed(AllocationSite allocation) {
			var pointed = ptrResult.pointedBy(allocation);
			return pointed.stream().flatMap(ptr -> ptr.getDependant().stream()).anyMatch(used -> {
				if (used instanceof Load) return true;
				if (used instanceof Store) return false;
				return ValueUtil.sideEffect(module, used);
			});
		}

		public boolean run() {
			var unusedInst = new HashSet<Instruction>();
			var unusedGlobal = new HashSet<GlobalVariable>();
			module.getFunctions().values().stream().flatMap(Function::allInstructions).forEach(inst -> {
				if (!(inst instanceof Alloca alloca)) return;
				if (actuallyUsed(alloca)) return;
				ChainedRemoveHelper.markUnused((Value) alloca, unusedInst);
			});
			module.getGlobalDeclarations().values().forEach(global -> {
				if (actuallyUsed(global)) return;
				unusedGlobal.add(global);
				ChainedRemoveHelper.markUnused(global, unusedInst);
			});

			var res = false;

			if (!unusedGlobal.isEmpty()) {
				res = true;
				unusedGlobal.forEach(module::removeGlobalVariable);
			}
			res |= ChainedRemoveHelper.cleanModule(module, unusedInst, query);

			if (res) query.invalidateAllAttributes(module);
			return res;
		}
	}
}
