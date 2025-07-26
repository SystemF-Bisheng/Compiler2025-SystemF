package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.*;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.optimization.pass.util.CodeMotionHelper;
import org.systemf.compiler.optimization.pass.util.MergeHelper;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.Tree;

import java.util.*;
import java.util.stream.Collectors;

public enum GlobalRemoveStore implements OptPass {
	INSTANCE;

	@Override
	public boolean run(org.systemf.compiler.ir.Module module) {
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

		private void collectRequiring(BasicBlock block) {
			var affected = new HashSet<Value>();
			for (var inst : block.instructions) {
				if (inst instanceof Load load) affected.addAll(ptrResult.pointTo(load.getPointer()));
				else if (ValueUtil.sideEffect(module, inst)) {
					affected = null;
					break;
				}
			}
			requiring.put(block, Optional.ofNullable(affected));
		}

		private void collectStoring(BasicBlock block) {
			storing.put(block, MergeHelper.constructStoreSet(block, module, ptrResult));
		}

		private boolean processBlock(BasicBlock block) {
			var res = false;
			var required = new HashSet<Value>();
			for (var iter = block.instructions.descendingIterator(); iter.hasNext(); ) {
				var inst = iter.next();
				if (inst instanceof Terminal) continue;
				if (!(inst instanceof Store store)) {
					if (inst instanceof Load load) required.addAll(ptrResult.pointTo(load.getPointer()));
					else if (ValueUtil.sideEffect(module, inst)) break;
					continue;
				}
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
					if (blockReachable.contains(ptrDef) && !domTree.subtree(block, lower)) continue;
				}
				var possibleRequire = blockReachable.stream().filter(succ -> succ != block).filter(succ -> {
					var succRequire = requiring.get(succ);
					if (succRequire.isEmpty()) return true;
					var requireSet = succRequire.get();
					return storeTo.stream().anyMatch(requireSet::contains);
				}).collect(Collectors.toSet());
				if (MergeHelper.blockingReachability(cfg, Collections.singleton(block), possibleRequire,
						Collections.singleton(lower))) continue;

				res = true;
				inst.unregister();
				iter.remove();
			}
			return res;
		}

		private boolean processFunction(Function function) {
			cfg = query.getAttribute(function, CFGAnalysisResult.class);
			belonging = CodeMotionHelper.getBelonging(function);
			var postDom = query.getAttribute(function, PostDominanceAnalysisResult.class);
			postDomTree = postDom.dominance();
			postDomRoot = postDomTree.getRoot();
			domTree = query.getAttribute(function, DominanceAnalysisResult.class).dominance();
			reachability = query.getAttribute(function, ReachabilityAnalysisResult.class);
			requiring = new HashMap<>();
			function.getBlocks().forEach(this::collectRequiring);
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
