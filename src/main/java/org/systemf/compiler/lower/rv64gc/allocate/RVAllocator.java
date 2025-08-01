package org.systemf.compiler.lower.rv64gc.allocate;

import org.systemf.compiler.lower.rv64gc.allocate.pass.RVInBlockSchedule;
import org.systemf.compiler.lower.rv64gc.allocate.pass.RVRegAlloc;
import org.systemf.compiler.lower.rv64gc.optimization.RVOptimizedResult;
import org.systemf.compiler.query.EntityProvider;
import org.systemf.compiler.query.QueryManager;

public enum RVAllocator implements EntityProvider<RVAllocatedResult> {
	INSTANCE;

	@Override
	public RVAllocatedResult produce() {
		var query = QueryManager.getInstance();
		var optimized = query.get(RVOptimizedResult.class);
		var module = optimized.module();
		query.invalidate(optimized);

		RVInBlockSchedule.INSTANCE.run(module);
		RVRegAlloc.INSTANCE.run(module);

		return new RVAllocatedResult(module);
	}
}
