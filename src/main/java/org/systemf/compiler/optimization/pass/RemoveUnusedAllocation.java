package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.PointerAnalysisResult;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Alloca;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.query.QueryManager;

import java.util.HashSet;

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
		private final HashSet<Instruction> unusedInst = new HashSet<>();
		private final HashSet<GlobalVariable> unusedGlobal = new HashSet<>();

		public RemoveUnusedAllocationContext(Module module) {
			this.module = module;
			this.ptrResult = query.getAttribute(module, PointerAnalysisResult.class);
		}

		private boolean actuallyUsed(Value allocation) {
			var pointed = ptrResult.pointedBy(allocation);
			return pointed.stream().flatMap(ptr -> ptr.getDependant().stream()).anyMatch(used -> {
				if (used instanceof Load) return true;
				if (used instanceof Store) return false;
				return ValueUtil.sideEffect(module, used);
			});
		}

		private void markUnused(Value value) {
			value.getDependant().forEach(inst -> {
				if (unusedInst.contains(inst)) return;
				unusedInst.add(inst);
				if (inst instanceof Value val) markUnused(val);
			});
		}

		private boolean cleanBlock(BasicBlock block) {
			var res = false;
			for (var iter = block.instructions.iterator(); iter.hasNext(); ) {
				var inst = iter.next();
				if (!unusedInst.contains(inst)) continue;
				inst.unregister();
				iter.remove();
				res = true;
			}
			return res;
		}

		private boolean cleanFunction(Function function) {
			var res = function.getBlocks().stream().map(this::cleanBlock).reduce(false, (a, b) -> a || b);
			if (res) query.invalidateAllAttributes(function);
			return res;
		}

		public boolean run() {
			module.getFunctions().values().stream().flatMap(Function::allInstructions).forEach(inst -> {
				if (!(inst instanceof Alloca alloca)) return;
				if (actuallyUsed(alloca)) return;
				markUnused(alloca);
			});
			module.getGlobalDeclarations().values().forEach(global -> {
				if (actuallyUsed(global)) return;
				unusedGlobal.add(global);
				markUnused(global);
			});

			var res = false;

			if (!unusedGlobal.isEmpty()) {
				res = true;
				unusedGlobal.forEach(module::removeGlobalVariable);
			}
			res |= module.getFunctions().values().stream().map(this::cleanFunction).reduce(false, (a, b) -> a || b);

			if (res) query.invalidateAllAttributes(module);
			return res;
		}
	}
}
