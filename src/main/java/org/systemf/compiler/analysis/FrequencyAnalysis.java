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
		private final Tree<BasicBlock> domTree;
		private final LoopAnalysisResult loops;
		private final Map<BasicBlock, Integer> frequency = new HashMap<>();
		private Set<BasicBlock> curLoop;

		public FrequencyAnalysisContext(Function function) {
			this.function = function;
			var query = QueryManager.getInstance();
			this.cfg = query.getAttribute(function, CFGAnalysisResult.class);
			var dom = query.getAttribute(function, DominanceAnalysisResult.class);
			this.domTree = dom.dominance();
			this.loops = query.getAttribute(function, LoopAnalysisResult.class);
		}

		private void initFrequency() {
			function.getBlocks().forEach(block -> frequency.put(block, 16));
		}

		private void propagateFrequency(BasicBlock block) {
			var succs = cfg.successors(block).stream().filter(curLoop::contains).toList();
			curLoop.remove(block);
			if (succs.isEmpty()) return;
			var curFreq = frequency.get(block);
			if (loops.isHead(block)) // Nested loop head, don't distribute frequency
				succs.forEach(succ -> frequency.put(succ, curFreq));
			else { // Nested if, distribute frequency by block size
				int sumSize = succs.stream().map(succ -> succ.instructions.size()).reduce(0, Integer::sum);
				for (var succ : succs) {
					if (succ == block) continue;
					var size = succ.instructions.size();
					frequency.put(succ, SaturationArithmetic.saturatedLerp(curFreq, size, sumSize));
				}
			}
		}

		private void processBlock(BasicBlock block) {
			if (loops.isHead(block)) {
				frequency.computeIfPresent(block, (_, value) -> SaturationArithmetic.saturatedMul(value, 8));
				curLoop = new HashSet<>(loops.getLoop(block));
				curLoop.stream().sorted(Comparator.comparingInt(domTree::getDfn)).forEach(this::propagateFrequency);
			}
			domTree.getChildren(block).forEach(this::processBlock);
		}

		public FrequencyAnalysisResult run() {
			initFrequency();
			processBlock(domTree.getRoot());
			return new FrequencyAnalysisResult(frequency);
		}
	}
}
