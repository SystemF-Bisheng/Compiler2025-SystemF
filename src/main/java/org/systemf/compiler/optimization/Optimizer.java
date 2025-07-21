package org.systemf.compiler.optimization;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.optimization.pass.*;
import org.systemf.compiler.query.EntityProvider;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.translator.IRTranslatedResult;

public enum Optimizer implements EntityProvider<OptimizedResult> {
	INSTANCE;

	private void foldAndCleanup(Module module) {
		boolean flag = true;
		while (flag) {
			flag = ConstantFold.INSTANCE.run(module);
			flag |= CondBrFold.INSTANCE.run(module);
			flag |= RemoveDeadBlock.INSTANCE.run(module); // Dominance analysis doesn't work with dead blocks
			flag |= MergeCommonValue.INSTANCE.run(module);
			flag |= RemoveUnusedValue.INSTANCE.run(module);
			flag |= RemoveSingleBr.INSTANCE.run(module);
			flag |= MergeChain.INSTANCE.run(module);
		}
	}

	@Override
	public OptimizedResult produce() {
		var query = QueryManager.getInstance();
		var translated = query.get(IRTranslatedResult.class);
		var module = translated.module();

		RemoveDeadBlock.INSTANCE.run(module);
		RemoveSingleBr.INSTANCE.run(module);
		MergeChain.INSTANCE.run(module);
		MemToReg.INSTANCE.run(module);

		foldAndCleanup(module);

		query.invalidate(translated);
		return new OptimizedResult(module);
	}
}