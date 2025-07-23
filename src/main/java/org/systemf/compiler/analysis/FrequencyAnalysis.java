package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.query.AttributeProvider;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.SaturationArithmetic;
import org.systemf.compiler.util.Tree;

import java.util.*;

public enum FrequencyAnalysis implements AttributeProvider<Function, FrequencyAnalysisResult> {
	INSTANCE;

	@Override
	public FrequencyAnalysisResult getAttribute(Function entity) {
		return new FrequencyAnalysisContext(entity).run();
	}

	private static class FrequencyAnalysisContext {
		private final Function function;
		private final CFGAnalysisResult cfg;
		private final ReachabilityAnalysisResult reachability;
		private final Tree<BasicBlock> domTree;
		private final LoopAnalysisResult loops;
		private final Map<BasicBlock, Integer> frequency = new HashMap<>();
		private final Map<BasicBlock, Integer> loopOccur = new HashMap<>();
		private Set<BasicBlock> curLoop;

		public FrequencyAnalysisContext(Function function) {
			this.function = function;
			var query = QueryManager.getInstance();
			this.cfg = query.getAttribute(function, CFGAnalysisResult.class);
			this.reachability = query.getAttribute(function, ReachabilityAnalysisResult.class);
			var dom = query.getAttribute(function, DominanceAnalysisResult.class);
			this.domTree = dom.dominance();
			this.loops = query.getAttribute(function, LoopAnalysisResult.class);
		}

		private void addOccur(BasicBlock block) {
			loopOccur.compute(block, (_, v) -> v == null ? 1 : v + 1);
		}

		private void removeOccur(BasicBlock block) {
			loopOccur.computeIfPresent(block, (_, v) -> v == 1 ? null : v - 1);
		}

		private boolean checkOccur(BasicBlock block) {
			return loopOccur.containsKey(block);
		}

		private void collectOccur() {
			loops.loops().values().stream().flatMap(Collection::stream).forEach(this::addOccur);
		}

		private void addFrequency(BasicBlock block, java.util.function.Function<BasicBlock, Integer> freq) {
			var toAdd = freq.apply(block);
			frequency.compute(block, (_, v) -> v == null ? toAdd : SaturationArithmetic.saturatedAdd(v, toAdd));
		}

		private void propagateFrequency(BasicBlock block) {
			var succs = cfg.successors(block).stream().filter(curLoop::contains)
					.filter(succ -> !domTree.subtree(succ, block)).toList();
			if (succs.isEmpty()) return;
			var curFreq = frequency.get(block);
			java.util.function.Function<BasicBlock, Integer> calc;
			if (checkOccur(block)) // Nested loop, don't distribute frequency
				calc = _ -> curFreq;
			else { // Nested if, distribute frequency by block size
				int sumSize = succs.stream().map(succ -> succ.instructions.size()).reduce(0, Integer::sum);
				calc = succ -> SaturationArithmetic.saturatedLerp(curFreq, succ.instructions.size(), sumSize);
			}

			for (var succ : succs) {
				if (succ == block) continue;
				addFrequency(succ, calc);
			}
		}

		private BasicBlock chooseNext(Set<BasicBlock> toPropagate) {
			for (var nxt : toPropagate) {
				if (toPropagate.stream().filter(blk -> blk != nxt).anyMatch(blk -> reachability.reachable(blk, nxt)))
					continue;
				return nxt;
			}
			return toPropagate.stream().min(Comparator.comparingInt(domTree::getDfn)).orElseThrow();
		}

		private void handleLoop(BasicBlock head, Set<BasicBlock> loop, boolean occurs) {
			var headNew = SaturationArithmetic.saturatedMul(frequency.getOrDefault(head, 0), 8);
			if (occurs) loop.forEach(this::removeOccur);
			loop.forEach(frequency::remove);
			frequency.put(head, headNew);
			curLoop = loop;
			var propagateOrder = new HashSet<>(curLoop);
			while (!propagateOrder.isEmpty()) {
				var next = chooseNext(propagateOrder);
				propagateOrder.remove(next);
				propagateFrequency(next);
			}
		}

		private void initFrequency() {
			var entry = function.getEntryBlock();
			frequency.put(entry, 2);
			handleLoop(entry, function.getBlocks(), false);
		}

		private void fillFrequency() {
			function.getBlocks().forEach(block -> frequency.putIfAbsent(block, 16));
		}

		private void processBlock(BasicBlock block) {
			if (loops.isHead(block)) handleLoop(block, loops.getLoop(block), true);
			domTree.getChildren(block).forEach(this::processBlock);
		}

		public FrequencyAnalysisResult run() {
			collectOccur();
			initFrequency();
			processBlock(domTree.getRoot());
			fillFrequency();
			return new FrequencyAnalysisResult(frequency);
		}
	}
}
