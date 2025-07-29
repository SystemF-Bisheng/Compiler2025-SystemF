package org.systemf.compiler.lower.rv64gc.optimization.pass;

import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.PotentialNonRepeatable;
import org.systemf.compiler.ir.value.instruction.PotentialSideEffect;
import org.systemf.compiler.lower.rv64gc.analysis.RVDominanceAnalysisResult;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.optimization.pass.util.MergeHelper;
import org.systemf.compiler.query.QueryManager;

public enum RVMergeCommonValue implements RVOptPass {
	INSTANCE;

	private boolean processFunction(org.systemf.compiler.ir.Module module, Function function) {
		var query = QueryManager.getInstance();
		var domTree = query.getAttribute(function, RVDominanceAnalysisResult.class).dominance();
		var res = MergeHelper.handleFunction(function, domTree,
				val -> !(val instanceof PotentialNonRepeatable || val instanceof PotentialSideEffect));
		if (res) query.invalidateAllAttributes(function);
		return res;
	}

	@Override
	public boolean run(RVModule rvModule) {
		var module = rvModule.module();
		var res = module.getFunctions().values().stream().map(func -> processFunction(module, func))
				.reduce(false, (a, b) -> a || b);
		if (res) QueryManager.getInstance().invalidateAllAttributes(module);
		return res;
	}
}
