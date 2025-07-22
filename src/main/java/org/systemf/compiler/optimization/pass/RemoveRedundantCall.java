package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.CallVoid;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.query.QueryManager;

/**
 * Remove CallVoid instructions without side effect
 */
public enum RemoveRedundantCall implements OptPass {
	INSTANCE;

	private boolean processBlock(Module module, BasicBlock block) {
		var res = false;
		for (var iter = block.instructions.iterator(); iter.hasNext(); ) {
			var inst = iter.next();
			if (!(inst instanceof CallVoid)) continue;
			if (ValueUtil.sideEffect(module, inst)) continue;
			inst.unregister();
			iter.remove();
			res = true;
		}
		return res;
	}

	private boolean processFunction(Module module, Function function) {
		var res = function.getBlocks().stream().map(block -> processBlock(module, block))
				.reduce(false, (a, b) -> a || b);
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
