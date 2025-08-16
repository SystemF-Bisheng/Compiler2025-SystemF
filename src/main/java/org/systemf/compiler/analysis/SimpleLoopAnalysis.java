package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.query.AttributeProvider;
import org.systemf.compiler.query.QueryManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Depend on: CFGAnalysis
 * <p>
 * Applicable to: IR
 */
public enum SimpleLoopAnalysis implements AttributeProvider<Function, SimpleLoopAnalysisResult> {
	INSTANCE;

	@Override
	public SimpleLoopAnalysisResult getAttribute(Function entity) {
		return new SimpleLoopAnalysisContext(entity).run();
	}

	private static class SimpleLoopAnalysisContext {
		private final Function function;
		private final CFGAnalysisResult cfg;
		private final List<SimpleLoopAnalysisResult.SimpleLoop> loops = new ArrayList<>();

		public SimpleLoopAnalysisContext(Function function) {
			this.function = function;
			this.cfg = QueryManager.getInstance().getAttribute(function, CFGAnalysisResult.class);
		}

		private boolean checkBody(BasicBlock head, BasicBlock body) {
			if (!(body.getTerminator() instanceof Br br)) return false;
			if (cfg.predecessors(body).size() != 1) return false;
			return br.getTarget() == head;
		}

		private void processBlock(BasicBlock block) {
			if (!(block.getTerminator() instanceof CondBr condBr)) return;
			var trueBlock = condBr.getTrueTarget();
			var falseBlock = condBr.getFalseTarget();
			if (checkBody(block, trueBlock) && falseBlock != block)
				loops.add(new SimpleLoopAnalysisResult.SimpleLoop(block, trueBlock, falseBlock));
			else if (checkBody(block, falseBlock) && trueBlock != block)
				loops.add(new SimpleLoopAnalysisResult.SimpleLoop(block, falseBlock, trueBlock));
		}

		public SimpleLoopAnalysisResult run() {
			function.getBlocks().forEach(this::processBlock);
			return new SimpleLoopAnalysisResult(loops);
		}
	}
}
