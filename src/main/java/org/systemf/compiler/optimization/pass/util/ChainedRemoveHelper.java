package org.systemf.compiler.optimization.pass.util;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.query.QueryManager;

import java.util.Set;

public class ChainedRemoveHelper {
	public static void markUnused(Value value, Set<Instruction> unusedInst) {
		value.getDependant().forEach(inst -> markUnused(inst, unusedInst));
	}

	public static void markUnused(Instruction inst, Set<Instruction> unusedInst) {
		if (!unusedInst.add(inst)) return;
		if (inst instanceof Value val) markUnused(val, unusedInst);
	}

	public static boolean cleanBlock(BasicBlock block, Set<Instruction> unusedInst) {
		var res = false;
		for (var iter = block.instructions.iterator(); iter.hasNext(); ) {
			var inst = iter.next();
			if (!unusedInst.contains(inst)) continue;
			inst.unregister();
			iter.remove();
			res = true;
		}
		return res;
	}

	public static boolean cleanFunction(Function function, Set<Instruction> unusedInst, QueryManager query) {
		var res = function.getBlocks().stream().map(block -> cleanBlock(block, unusedInst))
				.reduce(false, (a, b) -> a || b);
		if (res) query.invalidateAllAttributes(function);
		return res;
	}

	public static boolean cleanModule(Module module, Set<Instruction> unusedInst, QueryManager query) {
		var res = module.getFunctions().values().stream().map(func -> cleanFunction(func, unusedInst, query))
				.reduce(false, (a, b) -> a || b);
		if (res) query.invalidateAllAttributes(module);
		return res;
	}
}
