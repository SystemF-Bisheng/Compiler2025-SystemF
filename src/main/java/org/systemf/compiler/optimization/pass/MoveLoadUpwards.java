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
 * Depend on: DominanceAnalysis, PointerAnalysis, CFGAnalysis, ReachabilityAnalysis, PostDominanceAnalysis
 * <p>
 * Applicable to: IR
 */
public enum MoveLoadUpwards implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new MoveLoadUpwardsContext(module).run();
	}

	private static class MoveLoadUpwardsContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private final PointerAnalysisResult ptrResult;
		private Map<Instruction, BasicBlock> belonging;
		private CFGAnalysisResult cfg;
		private ReachabilityAnalysisResult reachability;
		private Map<BasicBlock, Optional<Set<Value>>> affecting;
		private Tree<BasicBlock> domTree;
		private Tree<BasicBlock> postDomTree;

		public MoveLoadUpwardsContext(Module module) {
			this.module = module;
			this.ptrResult = query.getAttribute(module, PointerAnalysisResult.class);
		}

		private boolean processBlock(BasicBlock block) {
			var res = false;
			var affected = new HashSet<Value>();
			for (var iter = block.instructions.iterator(); iter.hasNext(); ) {
				var inst = iter.next();
				if (MergeHelper.manipulateAffected(module, inst, affected, ptrResult)) break;
				if (!(inst instanceof Load load)) continue;
				var loadPtr = load.getPointer();
				var loadFrom = ptrResult.pointTo(loadPtr);
				if (loadFrom.stream().anyMatch(affected::contains)) continue;

				var depUpperBound = CodeMotionHelper.getUpperBound(inst, domTree, belonging);
				if (depUpperBound == block) continue;
				var upper = block;
				do {
					var newUpper = domTree.getParent(upper);
					if (!(
							// Unconditional (motion won't cause out-of-bound access)
							postDomTree.subtree(block, newUpper) &&
							// No affecting (motion won't cause wrong result)
							MergeHelper.affectingFree(newUpper, block, loadFrom, affecting, cfg, reachability)
					))
						break;
					upper = newUpper;
				} while (upper != depUpperBound);
				if (upper == block) continue;

				res = true;
				iter.remove();

				CodeMotionHelper.insertTail(upper, inst);
				belonging.put(inst, upper);
			}
			return res;
		}

		private boolean processFunction(Function function) {
			domTree = query.getAttribute(function, DominanceAnalysisResult.class).dominance();
			postDomTree = query.getAttribute(function, PostDominanceAnalysisResult.class).dominance();
			cfg = query.getAttribute(function, CFGAnalysisResult.class);
			reachability = query.getAttribute(function, ReachabilityAnalysisResult.class);
			affecting = MergeHelper.constructAffecting(module, function, ptrResult);
			belonging = BelongingHelper.getBelonging(function);
			var res = function.getBlocks().stream().sorted(Comparator.comparingInt(domTree::getDfn))
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
