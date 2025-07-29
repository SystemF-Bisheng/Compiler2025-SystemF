package org.systemf.compiler.lower.rv64gc.optimization;

import org.systemf.compiler.lower.rv64gc.lowering.RVLoweringResult;
import org.systemf.compiler.lower.rv64gc.optimization.pass.*;
import org.systemf.compiler.query.EntityProvider;
import org.systemf.compiler.query.QueryManager;

public enum RVOptimizer implements EntityProvider<RVOptimizedResult> {
	INSTANCE;

	@Override
	public RVOptimizedResult produce() {
		var query = QueryManager.getInstance();
		var lowered = query.get(RVLoweringResult.class);
		var module = lowered.module();
		query.invalidate(lowered);

		boolean flag = true;
		while (flag) {
			flag = RVRemoveUnusedValue.INSTANCE.run(module);
			flag |= RVMergeCommonValue.INSTANCE.run(module);
			flag |= RVConstantFold.INSTANCE.run(module);
			flag |= RVReduceStrength.INSTANCE.run(module);
			flag |= RVMergeArithmetic.INSTANCE.run(module);
		}

		return new RVOptimizedResult(module);
	}
}
