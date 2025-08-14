package org.systemf.compiler.analysis;

import org.systemf.compiler.analysis.util.BelongingHelper;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.ConstantInt;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Add;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.ICmp;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.query.AttributeProvider;
import org.systemf.compiler.query.QueryManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Depend on: CFGAnalysis
 * <p>
 * Applicable to: IR
 */
public enum SimpleForAnalysis implements AttributeProvider<Function, SimpleForAnalysisResult> {
	INSTANCE;

	@Override
	public SimpleForAnalysisResult getAttribute(Function entity) {
		return new SimpleForAnalysisContext(entity).run();
	}

	private static class SimpleForAnalysisContext {
		private final Function function;
		private final CFGAnalysisResult cfg;
		private final List<SimpleForAnalysisResult.SimpleFor> loops = new ArrayList<>();
		private final Map<Instruction, BasicBlock> belonging;

		public SimpleForAnalysisContext(Function function) {
			this.function = function;
			this.cfg = QueryManager.getInstance().getAttribute(function, CFGAnalysisResult.class);
			this.belonging = BelongingHelper.getBelonging(function);
		}

		private boolean checkBody(BasicBlock head, BasicBlock body) {
			if (!(body.getTerminator() instanceof Br br)) return false;
			return br.getTarget() == head;
		}

		private void processBlock(BasicBlock block) {
			if (!(block.getTerminator() instanceof CondBr condBr)) return;
			if (!(condBr.getCondition() instanceof ICmp cmpCond)) return;
			if (cmpCond.method != CompareOp.LT) return;

			BasicBlock body;
			Value loopI, loopBound;
			boolean inclusive;
			if (checkBody(block, condBr.getTrueTarget())) {
				body = condBr.getTrueTarget();
				loopI = cmpCond.getX();
				loopBound = cmpCond.getY();
				inclusive = false;
			} else if (checkBody(block, condBr.getFalseTarget())) {
				body = condBr.getFalseTarget();
				loopI = cmpCond.getY();
				loopBound = cmpCond.getX();
				inclusive = true;
			} else return;
			if (cfg.predecessors(body).size() != 1) return;

			if (!(loopI instanceof Phi iPhi)) return;
			if (belonging.get(iPhi) != block) return;
			var beginOpt = iPhi.getIncoming().entrySet().stream().filter(entry -> entry.getKey() != body)
					.map(Map.Entry::getValue)
					.map(Optional::of).reduce((x, y) -> {
						if (x.isEmpty() || y.isEmpty()) return x;
						if (ValueUtil.trivialInterchangeable(x.get(), y.get())) return x;
						return Optional.empty();
					}).orElseThrow();
			if (beginOpt.isEmpty()) return;
			var begin = beginOpt.get();

			if (loopBound instanceof Instruction && belonging.get(loopBound) == block) return;
			var end = loopBound;

			var next = iPhi.getIncoming().get(body);
			if (!(next instanceof Add nextAdd)) return;
			var addY = nextAdd.getY();
			if (!ValueUtil.isConstantInt(addY)) return;
			var step = (ConstantInt) addY;

			loops.add(new SimpleForAnalysisResult.SimpleFor(begin, end, step, inclusive, block, body));
		}

		public SimpleForAnalysisResult run() {
			function.getBlocks().forEach(this::processBlock);
			return new SimpleForAnalysisResult(loops);
		}
	}
}
