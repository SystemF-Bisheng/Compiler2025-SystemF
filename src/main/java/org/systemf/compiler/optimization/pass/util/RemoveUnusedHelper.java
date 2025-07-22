package org.systemf.compiler.optimization.pass.util;

import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.function.Predicate;

public class RemoveUnusedHelper {
	public static boolean handleFunction(Function function, Predicate<Instruction> rootFilter,
			Predicate<Value> valueFilter) {
		var res = false;
		var used = new HashSet<Value>();
		var worklist = new ArrayDeque<Instruction>();
		function.allInstructions().filter(rootFilter).forEach(worklist::push);
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
				if (!valueFilter.test(value)) continue;
				if (used.contains(value)) continue;

				res = true;
				inst.unregister();
				iter.remove();
			}
		return res;
	}
}
