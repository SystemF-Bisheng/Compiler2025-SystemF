package org.systemf.compiler.optimization;

import org.systemf.compiler.optimization.pass.MergeChain;
import org.systemf.compiler.optimization.pass.RemoveDeadBlock;
import org.systemf.compiler.optimization.pass.RemoveSingleBr;
import org.systemf.compiler.query.EntityProvider;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.translator.IRTranslatedResult;

public enum Optimizer implements EntityProvider<OptimizedResult> {
	INSTANCE;

	@Override
	public OptimizedResult produce() {
		var query = QueryManager.getInstance();
		var translated = query.get(IRTranslatedResult.class);
		var module = translated.module();

		RemoveDeadBlock.INSTANCE.run(module);
		MergeChain.INSTANCE.run(module);
		RemoveSingleBr.INSTANCE.run(module);

		query.invalidate(translated);
		return new OptimizedResult(module);
	}
}