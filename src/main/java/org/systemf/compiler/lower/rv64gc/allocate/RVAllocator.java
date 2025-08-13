package org.systemf.compiler.lower.rv64gc.allocate;

import org.systemf.compiler.lower.rv64gc.allocate.pass.RVInBlockSchedule;
import org.systemf.compiler.lower.rv64gc.allocate.pass.RVRegAlloc;
import org.systemf.compiler.lower.rv64gc.allocate.pass.RVSimplifyParMove;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.lower.rv64gc.optimization.RVOptimizedResult;
import org.systemf.compiler.lower.rv64gc.optimization.pass.RVMergeChain;
import org.systemf.compiler.lower.rv64gc.optimization.pass.RVRemoveSingleBr;
import org.systemf.compiler.query.EntityProvider;
import org.systemf.compiler.query.QueryManager;

public enum RVAllocator implements EntityProvider<RVAllocatedResult> {
	INSTANCE;

	private void cleanUp(RVModule module) {
		boolean flag = true;
		while (flag) {
			flag = RVRemoveSingleBr.INSTANCE.run(module);
			flag |= RVMergeChain.INSTANCE.run(module);
		}
	}

	@Override
	public RVAllocatedResult produce() {
		var query = QueryManager.getInstance();
		var optimized = query.get(RVOptimizedResult.class);
		var module = optimized.module();
		query.invalidate(optimized);

		RVInBlockSchedule.INSTANCE.run(module);
		RVRegAlloc.INSTANCE.run(module);
		RVSimplifyParMove.INSTANCE.run(module);
		cleanUp(module);

		return new RVAllocatedResult(module);
	}
}
