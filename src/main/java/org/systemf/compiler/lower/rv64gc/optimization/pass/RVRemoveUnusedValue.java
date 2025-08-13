package org.systemf.compiler.lower.rv64gc.optimization.pass;

import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.PotentialSideEffect;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.optimization.pass.util.RemoveUnusedHelper;
import org.systemf.compiler.query.QueryManager;

public enum RVRemoveUnusedValue implements RVOptPass {
	INSTANCE;

	private boolean processFunction(Function function) {
		var res = RemoveUnusedHelper.handleFunction(function, inst -> inst instanceof PotentialSideEffect,
				val -> !(val instanceof PotentialSideEffect));
		if (res) QueryManager.getInstance().invalidateAllAttributes(function);
		return res;
	}

	@Override
	public boolean run(RVModule rvModule) {
		var module = rvModule.module();
		var res = module.getFunctions().values().stream().map(this::processFunction).reduce(false, (a, b) -> a || b);
		if (res) {
			var query = QueryManager.getInstance();
			query.invalidateAllAttributes(module);
			query.invalidateAllAttributes(rvModule);
		}
		return res;
	}
}
