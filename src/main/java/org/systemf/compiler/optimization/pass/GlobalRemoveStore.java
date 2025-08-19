package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.*;
import org.systemf.compiler.analysis.util.BelongingHelper;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.optimization.pass.util.MergeHelper;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.Tree;

import java.util.*;

/**
 * Depend on: CFGAnalysis, PointerAnalysis, ReachabilityAnalysis
 * <p>
 * Applicable to: IR
 */
public enum GlobalRemoveStore implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new GlobalRemoveStoreContext(module).run();
	}

	private static class GlobalRemoveStoreContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private final PointerAnalysisResult ptrResult;
		private CFGAnalysisResult cfg;
		private ReachabilityAnalysisResult reachability;
		private Map<BasicBlock, Optional<Set<Value>>> requiring;
		private Map<Instruction, BasicBlock> belonging;
		private Map<BasicBlock, Set<Value>> storing;
		private BasicBlock postDomRoot;
		private Tree<BasicBlock> postDomTree;
		private Tree<BasicBlock> domTree;

		public GlobalRemoveStoreContext(Module module) {
			this.module = module;
			this.ptrResult = query.getAttribute(module, PointerAnalysisResult.class);
		}

		private void collectStoring(BasicBlock block) {
			storing.put(block, MergeHelper.constructStoreSet(block, module, ptrResult));
		}

		private boolean processBlock(BasicBlock block) {
			var res = false;
			var required = new HashSet<Value>();
			for (var iter = block.instructions.descendingIterator(); iter.hasNext(); ) {
				var inst = iter.next();
				if (MergeHelper.manipulateRequired(module, inst, required, ptrResult)) break;
				if (!(inst instanceof Store store)) continue;
				var storePtr = store.getDest();
				var storeTo = ptrResult.pointTo(storePtr);
				if (storeTo.stream().anyMatch(required::contains)) continue;

				var lower = postDomTree.getParent(block);
				while (lower != postDomRoot) {
					if (storing.get(lower).contains(storePtr)) break;
					lower = postDomTree.getParent(lower);
				}
				if (lower == postDomRoot) continue;

				var blockReachable = reachability.reachable().get(block);
				if (storePtr instanceof Instruction storePtrInst) {
					var ptrDef = belonging.get(storePtrInst);
					if (blockReachable.contains(ptrDef)) continue;
				}
				if (!MergeHelper.requiringFree(block, lower, storeTo, requiring, cfg, reachability)) continue;

				res = true;
				inst.unregister();
				iter.remove();
			}
			return res;
		}

		private boolean processFunction(Function function) {
			cfg = query.getAttribute(function, CFGAnalysisResult.class);
			belonging = BelongingHelper.getBelonging(function);
			var postDom = query.getAttribute(function, PostDominanceAnalysisResult.class);
			postDomTree = postDom.dominance();
			postDomRoot = postDomTree.getRoot();
			domTree = query.getAttribute(function, DominanceAnalysisResult.class).dominance();
			reachability = query.getAttribute(function, ReachabilityAnalysisResult.class);
			requiring = MergeHelper.constructRequiring(module, function, ptrResult);
			storing = new HashMap<>();
			function.getBlocks().forEach(this::collectStoring);

			var res = function.getBlocks().stream().sorted(Comparator.comparingInt(postDomTree::getDfn))
					.map(this::processBlock).reduce(false, (a, b) -> a || b);
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
