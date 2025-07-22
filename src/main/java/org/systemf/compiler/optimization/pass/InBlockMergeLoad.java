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

import java.util.HashMap;

public enum InBlockMergeLoad implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new InBlockMergeLoadContext(module).run();
	}

	private static class InBlockMergeLoadContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private final PointerAnalysisResult ptrResult;

		public InBlockMergeLoadContext(Module module) {
			this.module = module;
			this.ptrResult = query.getAttribute(module, PointerAnalysisResult.class);
		}

		private boolean processBlock(BasicBlock block) {
			var res = false;
			var loadMap = new HashMap<Value, Value>();
			for (var inst : block.instructions) {
				if (inst instanceof Load load) {
					var ptr = load.getPointer();
					if (loadMap.containsKey(ptr)) {
						load.replaceAllUsage(loadMap.get(ptr));
						res = true;
					} else loadMap.put(ptr, load);
				} else if (inst instanceof Store store) {
					var ptr = store.getDest();
					var affected = ptrResult.pointTo(ptr);
					for (var iter = loadMap.entrySet().iterator(); iter.hasNext(); ) {
						var entry = iter.next();
						var entryPtr = entry.getKey();
						var related = ptrResult.pointTo(entryPtr);
						if (affected.stream().anyMatch(related::contains)) iter.remove();
					}
					loadMap.put(ptr, store.getSrc());
				} else if (ValueUtil.sideEffect(module, inst)) loadMap.clear();
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
