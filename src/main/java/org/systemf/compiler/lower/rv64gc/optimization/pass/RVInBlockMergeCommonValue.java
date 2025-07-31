package org.systemf.compiler.lower.rv64gc.optimization.pass;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.PotentialNonRepeatable;
import org.systemf.compiler.ir.value.instruction.PotentialSideEffect;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.optimization.pass.util.MergeHelper;
import org.systemf.compiler.query.QueryManager;

public enum RVInBlockMergeCommonValue implements RVOptPass {
	INSTANCE;

	private boolean processBlock(BasicBlock block) {
		return MergeHelper.handleInBlock(block,
				val -> !(val instanceof PotentialNonRepeatable || val instanceof PotentialSideEffect));
	}

	private boolean processFunction(Function function) {
		var res = function.getBlocks().stream().map(this::processBlock).reduce(false, (a, b) -> a || b);
		if (res) QueryManager.getInstance().invalidateAllAttributes(function);
		return res;
	}

	@Override
	public boolean run(RVModule rvModule) {
		var module = rvModule.module();
		var res = module.getFunctions().values().stream().map(this::processFunction).reduce(false, (a, b) -> a || b);
		if (res) QueryManager.getInstance().invalidateAllAttributes(module);
		return res;
	}
}
