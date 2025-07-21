package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.PotentialSideEffect;
import org.systemf.compiler.query.QueryManager;

import java.util.ArrayDeque;
import java.util.HashSet;

public enum RemoveUnusedValue implements OptPass {
	INSTANCE;

	private boolean processFunction(Function function) {
		var res = false;
		var used = new HashSet<Value>();
		var worklist = new ArrayDeque<Instruction>();
		function.allInstructions().filter(inst -> inst instanceof PotentialSideEffect).forEach(worklist::push);
		while (!worklist.isEmpty()) {
			var inst = worklist.pop();
			if (inst instanceof Value val) used.add(val);
			inst.getDependency().stream().filter(dep -> dep instanceof Value).map(dep -> (Value) dep).forEach(val -> {
				if (used.contains(val)) return;
				used.add(val);
				if (val instanceof Instruction valInst) worklist.push(valInst);
			});
		}

		for (var block : function.getBlocks())
			for (var iter = block.instructions.iterator(); iter.hasNext(); ) {
				var inst = iter.next();
				if (!(inst instanceof Value value)) continue;
				if (inst instanceof PotentialSideEffect) continue;
				if (used.contains(value)) continue;

				res = true;
				inst.unregister();
				iter.remove();
			}
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