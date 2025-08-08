package org.systemf.compiler.lower.rv64gc.optimization.pass;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.PotentialSideEffect;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.Call;
import org.systemf.compiler.ir.value.instruction.terminal.Ret;
import org.systemf.compiler.ir.value.instruction.terminal.RetVoid;
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;
import org.systemf.compiler.lower.rv64gc.instruction.RVTailCall;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.query.QueryManager;

public enum RVFoldTailCall implements RVOptPass {
	INSTANCE;

	@Override
	public boolean run(RVModule rvModule) {
		return new RVFoldTailCallContext(rvModule).run();
	}

	private static class RVFoldTailCallContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;

		public RVFoldTailCallContext(RVModule rvModule) {
			this.module = rvModule.module();
		}

		private boolean processBlock(BasicBlock block) {
			return block.instructions.stream().takeWhile(inst -> !(inst instanceof Terminal))
					.filter(inst -> inst instanceof PotentialSideEffect).reduce((_, x) -> x)
					.filter(last -> last instanceof AbstractCall).map(last -> (AbstractCall) last).map(lastACall -> {
						var term = block.getTerminator();
						if (term instanceof Ret ret) {
							if (!(lastACall instanceof Call lastCall)) return false;
							if (ret.getReturnValue() != lastCall) return false;
							if (lastCall.getDependant().size() > 1) return false;

							ret.unregister();
							block.instructions.removeLast();
							block.insertInstruction(new RVTailCall(lastCall.getFunction(), lastCall.getArgs()));

							lastCall.unregister();
							block.instructions.remove(lastCall);
							return true;
						} else if (term instanceof RetVoid retVoid) {
							if (lastACall instanceof Value lastValue && !lastValue.getDependant().isEmpty())
								return false;

							retVoid.unregister();
							block.instructions.removeLast();
							block.insertInstruction(new RVTailCall(lastACall.getFunction(), lastACall.getArgs()));

							lastACall.unregister();
							block.instructions.remove(lastACall);
							return true;
						}
						return false;
					}).orElse(false);
		}

		private boolean processFunction(Function function) {
			var res = function.getBlocks().stream().map(this::processBlock).reduce(false, (a, b) -> a || b);
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
