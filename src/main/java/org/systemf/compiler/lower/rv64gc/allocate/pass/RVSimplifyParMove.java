package org.systemf.compiler.lower.rv64gc.allocate.pass;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.lower.rv64gc.instruction.RVParallelMove;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.lower.rv64gc.util.RVRegUtil;
import org.systemf.compiler.query.QueryManager;

import java.util.ArrayList;

public enum RVSimplifyParMove {
	INSTANCE;

	public void run(RVModule rvModule) {
		new RVSimplifyParMoveContext(rvModule).run();
	}

	private static class RVSimplifyParMoveContext {
		private final QueryManager query = QueryManager.getInstance();
		private final RVModule rvModule;
		private final Module module;

		public RVSimplifyParMoveContext(RVModule rvModule) {
			this.rvModule = rvModule;
			this.module = rvModule.module();
		}

		private void processBlock(BasicBlock block) {
			for (var inst : block.instructions) {
				if (!(inst instanceof RVParallelMove parMove)) continue;
				var toRemove = new ArrayList<Value>();
				parMove.getMoves().forEach((to, from) -> {
					var toPos = RVRegUtil.positionOf(rvModule, to);
					var fromPos = RVRegUtil.positionOf(rvModule, from);
					if (RVRegUtil.needToMove(toPos, to.getType(), fromPos, from.getType())) return;
					toRemove.add(to);
				});
				toRemove.forEach(parMove::removeMove);
			}
			for (var iter = block.instructions.iterator(); iter.hasNext(); ) {
				var inst = iter.next();
				if (!(inst instanceof RVParallelMove parMove)) continue;
				if (!parMove.getMoves().isEmpty()) continue;
				parMove.unregister();
				iter.remove();
			}
		}

		private void processFunction(Function function) {
			function.getBlocks().forEach(this::processBlock);
			query.invalidateAllAttributes(function);
		}

		private void run() {
			module.getFunctions().values().forEach(this::processFunction);
			query.invalidateAllAttributes(module);
			query.invalidateAllAttributes(rvModule);
		}
	}
}
