package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.PotentialSideEffect;
import org.systemf.compiler.query.QueryManager;

// TODO: Reimplement this with graph reachability to remove loops
public enum RemoveUnusedValue implements OptPass {
	INSTANCE;

	private boolean processBlock(BasicBlock block) {
		boolean res = false;
		for (var iter = block.instructions.iterator(); iter.hasNext(); ) {
			var inst = iter.next();
			if (!(inst instanceof Value value)) continue;
			if (inst instanceof PotentialSideEffect) continue;

			var dependant = value.getDependant();
			var depCnt = dependant.size();
			if (dependant.contains(value)) --depCnt;
			if (depCnt > 0) continue;

			res = true;
			inst.unregister();
			iter.remove();
		}
		return res;
	}

	private boolean processFunction(Function function) {
		var res = function.getBlocks().stream().map(this::processBlock).reduce(false, (a, b) -> a || b);
		if (res) QueryManager.getInstance().invalidateAllAttributes(function);
		return res;
	}

	@Override
	public boolean run(Module module) {
		var res = module.getFunctions().values().stream().map(this::processFunction).reduce(false, (a, b) -> a || b);
		if (res) QueryManager.getInstance().invalidateAllAttributes(module);
		return res;
	}
}