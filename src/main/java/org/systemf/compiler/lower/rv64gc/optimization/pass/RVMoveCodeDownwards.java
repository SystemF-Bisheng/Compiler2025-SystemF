package org.systemf.compiler.lower.rv64gc.optimization.pass;

import org.systemf.compiler.analysis.util.BelongingHelper;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.lower.rv64gc.analysis.RVDominanceAnalysisResult;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.lower.rv64gc.optimization.util.RVCodeMotionHelper;
import org.systemf.compiler.optimization.pass.util.CodeMotionHelper;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.Tree;

import java.util.Comparator;
import java.util.Map;

public enum RVMoveCodeDownwards implements RVOptPass {
	INSTANCE;

	@Override
	public boolean run(RVModule rvModule) {
		return new RVMoveCodeDownwardsContext(rvModule).run();
	}

	private static class RVMoveCodeDownwardsContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private final Map<BasicBlock, Integer> frequency;
		private Map<Instruction, BasicBlock> belonging;
		private Tree<BasicBlock> domTree;

		public RVMoveCodeDownwardsContext(RVModule rvModule) {
			this.module = rvModule.module();
			this.frequency = rvModule.frequency();
		}

		private boolean processBlock(BasicBlock block) {
			var res = false;
			for (var iter = block.instructions.listIterator(block.instructions.size()); iter.hasPrevious(); ) {
				var inst = iter.previous();
				if (RVCodeMotionHelper.checkNonMobile(inst)) continue;

				var lowerBound = CodeMotionHelper.getLowerBound(inst, domTree, belonging);
				var bestLower = CodeMotionHelper.findBestLower(block, lowerBound, domTree, frequency);
				if (bestLower == block) continue;

				res = true;
				iter.remove();
				CodeMotionHelper.insertHead(bestLower, inst);
				belonging.put(inst, bestLower);
			}
			return res;
		}

		private boolean processFunction(Function function) {
			domTree = query.getAttribute(function, RVDominanceAnalysisResult.class).dominance();
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
