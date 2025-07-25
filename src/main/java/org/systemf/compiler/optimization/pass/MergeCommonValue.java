package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.optimization.pass.util.MergeHelper;
import org.systemf.compiler.query.QueryManager;

/**
 * Merge repeatable and side-effect-free instructions with trivially interchangeable arguments if possible
 */
public enum MergeCommonValue implements OptPass {
	INSTANCE;

	private boolean processFunction(Module module, Function function) {
		var res = MergeHelper.handleFunction(function,
				val -> ValueUtil.repeatable(module, val) && !ValueUtil.sideEffect(module, val));
		if (res) QueryManager.getInstance().invalidateAllAttributes(function);
		return res;
	}

	@Override
	public boolean run(Module module) {
		var res = module.getFunctions().values().stream().map(func -> processFunction(module, func))
				.reduce(false, (a, b) -> a || b);
		if (res) QueryManager.getInstance().invalidateAllAttributes(module);
		return res;
	}
}
