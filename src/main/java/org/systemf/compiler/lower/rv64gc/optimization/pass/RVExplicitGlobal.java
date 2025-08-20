package org.systemf.compiler.lower.rv64gc.optimization.pass;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.lower.rv64gc.instruction.RVLoadAddress;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.optimization.pass.util.CodeMotionHelper;
import org.systemf.compiler.query.QueryManager;

import java.util.ArrayList;
import java.util.List;

public enum RVExplicitGlobal implements RVOptPass {
	INSTANCE;

	@Override
	public boolean run(RVModule rvModule) {
		if (new RVExplicitGlobalContext(rvModule.module()).run()) {
			QueryManager.getInstance().invalidateAllAttributes(rvModule);
			return true;
		}
		return false;
	}

	private static class RVExplicitGlobalContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private final List<Instruction> toEntry = new ArrayList<>();

		public RVExplicitGlobalContext(Module module) {
			this.module = module;
		}

		private boolean processBlock(BasicBlock block) {
			var res = false;
			for (var inst : block.instructions) {
				if (inst instanceof RVLoadAddress) continue;
				for (var dep : inst.getDependency()) {
					if (!(dep instanceof GlobalVariable global)) continue;
					var newGlobal = new RVLoadAddress(global, module.getNonConflictName(global.getName() + "Addr"));
					toEntry.add(newGlobal);
					inst.replaceAll(global, newGlobal);
					res = true;
				}
			}
			return res;
		}

		private boolean processFunction(Function function) {
			toEntry.clear();
			var res = function.getBlocks().stream().map(this::processBlock).reduce(false, (a, b) -> a || b);
			if (res) {
				query.invalidateAllAttributes(function);
				CodeMotionHelper.insertEntry(function, toEntry.toArray(Instruction[]::new));
			}
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
