package org.systemf.compiler.optimization;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.optimization.pass.*;
import org.systemf.compiler.query.EntityProvider;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.translator.IRTranslatedResult;

public enum Optimizer implements EntityProvider<OptimizedResult> {
	INSTANCE;

	private boolean fastValueFoldOnce(Module module) {
		boolean flag = ConstantFold.INSTANCE.run(module);
		flag |= CondBrFold.INSTANCE.run(module);
		flag |= CanonicalizeValue.INSTANCE.run(module);
		flag |= RemoveDeadBlock.INSTANCE.run(module); // Dominance analysis doesn't work with dead blocks
		flag |= MergeArithmetic.INSTANCE.run(module);
		flag |= MergeCommonValue.INSTANCE.run(module);
		flag |= InBlockMergeLoad.INSTANCE.run(module);
		flag |= RemoveUnusedValue.INSTANCE.run(module);
		flag |= RemoveUnusedAllocation.INSTANCE.run(module);
		flag |= InBlockRemoveStore.INSTANCE.run(module);
		flag |= RemoveRedundantCall.INSTANCE.run(module);
		return flag;
	}

	private boolean slowValueFoldOnce(Module module) {
		boolean flag = RemoveDeadBlock.INSTANCE.run(module);
		flag |= MergeCondBr.INSTANCE.run(module);
		flag |= RemoveDeadBlock.INSTANCE.run(module);
		flag |= GlobalMergeLoad.INSTANCE.run(module);
		flag |= GlobalRemoveStore.INSTANCE.run(module);
		return flag;
	}

	private void fastValueFold(Module module) {
		boolean flag = true;
		while (flag) flag = fastValueFoldOnce(module);
	}

	private boolean cfgSimplifyOnce(Module module) {
		boolean flag = RemoveSingleBr.INSTANCE.run(module);
		flag |= MergeChain.INSTANCE.run(module);
		return flag;
	}

	private void fastValueAndCFGFold(Module module) {
		boolean flag = true;
		while (flag) {
			flag = fastValueFoldOnce(module);
			flag |= cfgSimplifyOnce(module);
		}
	}

	private void valueClean(Module module) {
		do fastValueFold(module); while (slowValueFoldOnce(module));
	}

	private void valueAndBlockClean(Module module) {
		do fastValueAndCFGFold(module); while (slowValueFoldOnce(module));
	}

	private void codeMotion(Module module) {
		while (MoveCodeUpwards.INSTANCE.run(module)) valueClean(module);
		while (MoveCodeDownwards.INSTANCE.run(module)) valueAndBlockClean(module);
		valueAndBlockClean(module);
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

		valueAndBlockClean(module);

		codeMotion(module);

		RemoveUnusedFunction.INSTANCE.run(module);

		query.invalidate(translated);
		return new OptimizedResult(module);
	}
}