package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.PointerAnalysisResult;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.optimization.pass.util.MergeHelper;
import org.systemf.compiler.query.QueryManager;

import java.util.HashMap;

/**
 * Merge multiple identical loads in one block
 */
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
						continue;
					}
				}
				MergeHelper.manipulateLoadMap(inst, loadMap, module, ptrResult);
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
