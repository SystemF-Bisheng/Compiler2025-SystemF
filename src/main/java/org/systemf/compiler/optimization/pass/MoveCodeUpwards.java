package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.DominanceAnalysisResult;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.optimization.pass.util.CodeMotionHelper;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.Tree;

import java.util.Comparator;
import java.util.Map;

public enum MoveCodeUpwards implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new MoveCodeUpwardsContext(module).run();
	}

	private static class MoveCodeUpwardsContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private Map<Instruction, BasicBlock> belonging;
		private Tree<BasicBlock> domTree;

		public MoveCodeUpwardsContext(Module module) {
			this.module = module;
		}

		private boolean processBlock(BasicBlock block) {
			var res = false;
			for (var iter = block.instructions.iterator(); iter.hasNext(); ) {
				var inst = iter.next();
				if (inst instanceof Terminal) continue;
				if (ValueUtil.sideEffect(module, inst)) continue;
				if (ValueUtil.positionSensitive(module, inst)) continue;
				var upperBound = CodeMotionHelper.getUpperBound(inst, domTree, belonging);
				if (upperBound == block) continue;
				res = true;
				iter.remove();

				CodeMotionHelper.insertTail(upperBound, inst);
				belonging.put(inst, upperBound);
			}
			return res;
		}

		private boolean processFunction(Function function) {
			domTree = query.getAttribute(function, DominanceAnalysisResult.class).dominance();
			belonging = CodeMotionHelper.getBelonging(function);
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
