package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.CFGAnalysisResult;
import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.ir.value.instruction.terminal.Unreachable;
import org.systemf.compiler.optimization.pass.util.ChainedRemoveHelper;
import org.systemf.compiler.query.QueryManager;

import java.util.HashSet;

/**
 * Mark blocks that end with unreachable and propagate unreachability on CFG.
 * Then remove unreachable blocks, jumps and functions whose entry is marked unreachable.
 * <p>
 * Depend on: CFGAnalysis
 * <p>
 * Applicable to: IR
 */
public enum RemoveUnreachable implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new RemoveUnreachableContext(module).run();
	}

	private static class RemoveUnreachableContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private final HashSet<Function> unreachableFunction = new HashSet<>();
		private final HashSet<BasicBlock> unreachableBlock = new HashSet<>();
		private IRBuilder builder;
		private CFGAnalysisResult cfg;

		public RemoveUnreachableContext(Module module) {
			this.module = module;
		}

		private void markUnreachable(BasicBlock block) {
			if (!unreachableBlock.add(block)) return;
			for (var pred : cfg.predecessors(block)) {
				if (pred == block) continue;
				var term = pred.getTerminator();
				if (term instanceof Br) markUnreachable(pred);
				else if (term instanceof CondBr condBr) {
					BasicBlock realTarget;
					if (condBr.getTrueTarget() == block) realTarget = condBr.getFalseTarget();
					else realTarget = condBr.getTrueTarget();
					condBr.unregister();
					pred.instructions.removeLast();
					builder.attachToBlockTail(pred);
					builder.buildBr(realTarget);

					cfg.successors(pred).remove(block);
				}
			}
			cfg.predecessors(block).clear();
		}

		private boolean processFunction(Function function) {
			unreachableBlock.clear();
			cfg = query.getAttribute(function, CFGAnalysisResult.class);
			function.getBlocks().stream().filter(block -> block.getTerminator() instanceof Unreachable)
					.forEach(this::markUnreachable);
			var res = !unreachableBlock.isEmpty();
			if (unreachableBlock.contains(function.getEntryBlock())) unreachableFunction.add(function);
			else unreachableBlock.forEach(block -> {
				block.instructions.forEach(Instruction::unregister);
				block.instructions.clear();
				function.deleteBlock(block);
			});
			if (res) query.invalidateAllAttributes(function);
			return res;
		}

		public boolean run() {
			try (var builder = new IRBuilder(module)) {
				unreachableFunction.clear();

				this.builder = builder;
				var res = module.getFunctions().values().stream().map(this::processFunction)
						.reduce(false, (a, b) -> a || b);
				if (!unreachableFunction.isEmpty()) {
					res = true;
					var unusedInst = new HashSet<Instruction>();
					unreachableFunction.forEach(func -> ChainedRemoveHelper.markUnused(func, unusedInst));
					unreachableFunction.stream().flatMap(Function::allInstructions).forEach(Instruction::unregister);
					unreachableFunction.forEach(module::removeFunction);
					ChainedRemoveHelper.cleanModule(module, unusedInst, query);
				}
				if (res) query.invalidateAllAttributes(module);
				return res;
			}
		}
	}
}
