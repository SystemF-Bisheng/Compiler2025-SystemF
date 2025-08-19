package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.DominanceAnalysisResult;
import org.systemf.compiler.analysis.PostDominanceAnalysisResult;
import org.systemf.compiler.analysis.util.BelongingHelper;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.FDiv;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.SDiv;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.SRem;
import org.systemf.compiler.optimization.pass.util.CodeMotionHelper;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.Tree;

import java.util.Comparator;
import java.util.Map;

/**
 * Depend on: DominanceAnalysis, PostDominanceAnalysis, FunctionSideEffectAnalysis
 * <p>
 * Applicable to: IR
 */
public enum MoveDivUpwards implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new MoveDivUpwardsContext(module).run();
	}

	private static class MoveDivUpwardsContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private Map<Instruction, BasicBlock> belonging;
		private Tree<BasicBlock> domTree;
		private Tree<BasicBlock> postDomTree;

		public MoveDivUpwardsContext(Module module) {
			this.module = module;
		}

		private boolean processBlock(BasicBlock block) {
			var res = false;
			for (var iter = block.instructions.iterator(); iter.hasNext(); ) {
				var inst = iter.next();
				if (!(inst instanceof SDiv || inst instanceof SRem || inst instanceof FDiv)) continue;

				var depUpperBound = CodeMotionHelper.getUpperBound(inst, domTree, belonging);
				if (depUpperBound == block) continue;
				var upper = block;
				do {
					var newUpper = domTree.getParent(upper);
					if (!postDomTree.subtree(block, newUpper)) break;
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
