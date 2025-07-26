package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.DominanceAnalysisResult;
import org.systemf.compiler.analysis.FrequencyAnalysisResult;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.PotentialPositionSensitive;
import org.systemf.compiler.ir.value.instruction.PotentialSideEffect;
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;
import org.systemf.compiler.optimization.pass.util.CodeMotionHelper;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.Tree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public enum MoveCodeDownwards implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new MoveCodeDownwardsContext(module).run();
	}

	private static class MoveCodeDownwardsContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private FrequencyAnalysisResult frequency;
		private Map<Instruction, BasicBlock> belonging;
		private Tree<BasicBlock> domTree;

		public MoveCodeDownwardsContext(Module module) {
			this.module = module;
		}

		private boolean processBlock(BasicBlock block) {
			var res = false;
			for (var iter = block.instructions.listIterator(block.instructions.size()); iter.hasPrevious(); ) {
				var inst = iter.previous();
				if (inst instanceof Terminal) continue;
				if (inst instanceof PotentialSideEffect) continue;
				if (inst instanceof PotentialPositionSensitive) continue;

				var lowerBound = CodeMotionHelper.getLowerBound(inst, domTree, belonging);
				var possibleLower = new ArrayList<BasicBlock>();
				while (true) {
					if (lowerBound == null) break;
					possibleLower.add(lowerBound);
					if (lowerBound == block) break;
					lowerBound = domTree.getParent(lowerBound);
				}
				if (possibleLower.isEmpty()) continue;
				var bestLower = possibleLower.stream().min(Comparator.comparingInt(lower -> frequency.frequency(lower)))
						.orElseThrow();
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
			domTree = query.getAttribute(function, DominanceAnalysisResult.class).dominance();
			belonging = CodeMotionHelper.getBelonging(function);
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
