package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.query.AttributeProvider;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.Tree;

import java.util.*;

public enum LoopAnalysis implements AttributeProvider<Function, LoopAnalysisResult> {
	INSTANCE;

	@Override
	public LoopAnalysisResult getAttribute(Function entity) {
		return new LoopAnalysisContext(entity).run();
	}

	private static class LoopAnalysisContext {
		private final Function function;
		private final CFGAnalysisResult cfg;
		private final Tree<BasicBlock> domTree;
		private final Set<BasicBlock> loopHeads = new HashSet<>();
		private final Set<BasicBlock> handledLoopHeads = new HashSet<>();
		private final Map<BasicBlock, Set<BasicBlock>> loops = new HashMap<>();
		private BasicBlock curHead;
		private Set<BasicBlock> curLoop;

		public LoopAnalysisContext(Function function) {
			this.function = function;
			var query = QueryManager.getInstance();
			this.cfg = query.getAttribute(function, CFGAnalysisResult.class);
			var dom = query.getAttribute(function, DominanceAnalysisResult.class);
			this.domTree = dom.dominance();
		}

		private void collectReturn(BasicBlock block) {
			for (var succ : cfg.successors(block)) {
				if (!domTree.subtree(succ, block)) continue;
				loopHeads.add(succ);
			}
		}

		private void collectReturn() {
			function.getBlocks().forEach(this::collectReturn);
		}

		private boolean checkReachable(BasicBlock from, BasicBlock to) {
			var visited = new HashSet<BasicBlock>();
			var queue = new ArrayDeque<BasicBlock>();
			queue.offer(from);
			while (!queue.isEmpty()) {
				var head = queue.poll();
				if (handledLoopHeads.contains(head)) continue;
				if (!visited.add(head)) continue;
				if (head == to) return true;
				cfg.successors(head).forEach(queue::offer);
			}
			return false;
		}

		private void collectLoop(BasicBlock cur) {
			if (checkReachable(cur, curHead)) curLoop.add(cur);
			domTree.getChildren(cur).forEach(this::collectLoop);
		}

		private void discoverLoop(BasicBlock block) {
			if (loopHeads.contains(block)) {
				curHead = block;
				curLoop = new HashSet<>();
				collectLoop(block);
				loops.put(block, curLoop);
				handledLoopHeads.add(block);
			}
			domTree.getChildren(block).forEach(this::discoverLoop);
		}

		public LoopAnalysisResult run() {
			collectReturn();
			discoverLoop(domTree.getRoot());
			return new LoopAnalysisResult(loops);
		}
	}
}
