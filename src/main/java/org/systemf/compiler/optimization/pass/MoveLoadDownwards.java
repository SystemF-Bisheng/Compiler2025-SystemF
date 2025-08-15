package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.*;
import org.systemf.compiler.analysis.util.BelongingHelper;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.optimization.pass.util.CodeMotionHelper;
import org.systemf.compiler.optimization.pass.util.MergeHelper;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.Tree;

import java.util.*;

/**
 * Depend on: FrequencyAnalysis, DominanceAnalysis, PointerAnalysis, CFGAnalysis, ReachabilityAnalysis
 * <p>
 * Applicable to: IR
 */
public enum MoveLoadDownwards implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new MoveLoadDownwardsContext(module).run();
	}

	private static class MoveLoadDownwardsContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private final PointerAnalysisResult ptrResult;
		private FrequencyAnalysisResult frequency;
		private Map<Instruction, BasicBlock> belonging;
		private CFGAnalysisResult cfg;
		private ReachabilityAnalysisResult reachability;
		private Map<BasicBlock, Optional<Set<Value>>> affecting;
		private Tree<BasicBlock> domTree;

		public MoveLoadDownwardsContext(Module module) {
			this.module = module;
			this.ptrResult = query.getAttribute(module, PointerAnalysisResult.class);
		}

		private boolean processBlock(BasicBlock block) {
			var res = false;
			var affected = new HashSet<Value>();
			for (var iter = block.instructions.listIterator(block.instructions.size()); iter.hasPrevious(); ) {
				var inst = iter.previous();
				if (MergeHelper.manipulateAffected(module, inst, affected, ptrResult)) break;
				if (!(inst instanceof Load load)) continue;
				var loadPtr = load.getPointer();
				var loadFrom = ptrResult.pointTo(loadPtr);
				if (loadFrom.stream().anyMatch(affected::contains)) continue;

				var lowerBound = CodeMotionHelper.getLowerBound(inst, domTree, belonging);
				var bestLower = CodeMotionHelper.findBestLower(block, lowerBound,
						lower -> MergeHelper.affectingFree(block, lower, loadFrom, affecting, cfg, reachability
						), domTree, frequency);
				if (bestLower == block) continue;

				res = true;
				iter.remove();
				CodeMotionHelper.insertHead(bestLower, inst);
				belonging.put(inst, bestLower);
			}
			return res;
		}

		private boolean processFunction(Function function) {
			frequency = query.getAttribute(function, FrequencyAnalysisResult.class);
			cfg = query.getAttribute(function, CFGAnalysisResult.class);
			reachability = query.getAttribute(function, ReachabilityAnalysisResult.class);
			affecting = MergeHelper.constructAffecting(module, function, ptrResult);
			domTree = query.getAttribute(function, DominanceAnalysisResult.class).dominance();
			belonging = BelongingHelper.getBelonging(function);
			var res = function.getBlocks().stream().sorted(Comparator.comparingInt(domTree::getDfn).reversed())
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
