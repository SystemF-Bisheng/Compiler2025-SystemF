package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.PointerAnalysisResult;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.query.QueryManager;

import java.util.HashSet;

/**
 * Remove useless stores in-block
 */
public enum InBlockRemoveStore implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new InBlockRemoveStoreContext(module).run();
	}

	private static class InBlockRemoveStoreContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private final PointerAnalysisResult ptrResult;

		public InBlockRemoveStoreContext(Module module) {
			this.module = module;
			this.ptrResult = query.getAttribute(module, PointerAnalysisResult.class);
		}

		private boolean processBlock(BasicBlock block) {
			var res = false;
			var storeSet = new HashSet<Value>();
			for (var instIter = block.instructions.descendingIterator(); instIter.hasNext(); ) {
				var inst = instIter.next();
				if (inst instanceof Load load) {
					var ptr = load.getPointer();
					var affected = ptrResult.pointTo(ptr);
					for (var iter = storeSet.iterator(); iter.hasNext(); ) {
						var storePtr = iter.next();
						var related = ptrResult.pointTo(storePtr);
						if (affected.stream().anyMatch(related::contains)) iter.remove();
					}
				} else if (inst instanceof Store store) {
					var ptr = store.getDest();
					if (storeSet.contains(ptr)) {
						res = true;
						store.unregister();
						instIter.remove();
					} else storeSet.add(ptr);
				} else if (ValueUtil.sideEffect(module, inst)) storeSet.clear();
			}
			return res;
		}

		private boolean processFunction(Function function) {
			var res = function.getBlocks().stream().map(this::processBlock).reduce(false, (a, b) -> a || b);
			if (res) query.invalidateAllAttributes(function);
			return res;
		}

		public boolean run() {
			var res = module.getFunctions().values().stream().map(this::processFunction)
					.reduce(false, (a, b) -> a || b);
			if (res) query.invalidateAllAttributes(module);
			return res;
		}
	}
}
