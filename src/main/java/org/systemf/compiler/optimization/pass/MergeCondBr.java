package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.DominanceAnalysisResult;
import org.systemf.compiler.analysis.ReachabilityAnalysisResult;
import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.Tree;

public enum MergeCondBr implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new MergeCondBrContext(module).run();
	}

	private static class MergeCondBrContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private IRBuilder builder;
		private Tree<BasicBlock> domTree;
		private ReachabilityAnalysisResult reachability;

		public MergeCondBrContext(Module module) {
			this.module = module;
		}

		private boolean processBlock(BasicBlock block) {
			if (!(block.getTerminator() instanceof CondBr condBr)) return false;
			var cond = condBr.getCondition();
			for (var parent = domTree.getParent(block); parent != null; parent = domTree.getParent(parent)) {
				if (!(parent.getTerminator() instanceof CondBr parentCondBr)) continue;
				var parentCond = parentCondBr.getCondition();
				if (cond != parentCond) continue;

				BasicBlock realTarget = null;
				if (!reachability.reachable(parentCondBr.getTrueTarget(), block)) realTarget = condBr.getFalseTarget();
				else if (!reachability.reachable(parentCondBr.getFalseTarget(), block))
					realTarget = condBr.getTrueTarget();
				if (realTarget == null) continue;

				condBr.unregister();
				block.instructions.removeLast();
				builder.attachToBlockTail(block);
				builder.buildBr(realTarget);
				return true;
			}
			return false;
		}

		private boolean processFunction(Function function) {
			this.domTree = query.getAttribute(function, DominanceAnalysisResult.class).dominance();
			this.reachability = query.getAttribute(function, ReachabilityAnalysisResult.class);
			var res = function.getBlocks().stream().map(this::processBlock).reduce(false, (a, b) -> a || b);
			if (res) query.invalidateAllAttributes(function);
			return res;
		}

		public boolean run() {
			try (var builder = new IRBuilder(module)) {
				this.builder = builder;
				var res = module.getFunctions().values().stream().map(this::processFunction)
						.reduce(false, (a, b) -> a || b);
				if (res) query.invalidateAllAttributes(module);
				return res;
			}
		}
	}
}
