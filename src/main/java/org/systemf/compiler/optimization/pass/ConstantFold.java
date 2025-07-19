package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.IRFolder;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.query.QueryManager;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;

public enum ConstantFold implements OptPass {
	INSTANCE;

	private boolean processFunction(Function function, IRFolder folder) {
		boolean result = false;
		Deque<Instruction> worklist = function.getBlocks().stream().flatMap(block -> block.instructions.stream())
				.filter(inst -> inst instanceof Value).collect(Collectors.toCollection(ArrayDeque::new));
		while (!worklist.isEmpty()) {
			var val = worklist.poll();
			var folded = val.accept(folder);
			if (folded.isPresent()) {
				result = true;
				var value = (Value) val;
				value.getDependant().stream().filter(inst -> inst instanceof Value).forEach(worklist::offer);
				value.replaceAllUsage((Constant) folded.get());
			}
		}

		if (result) QueryManager.getInstance().invalidateAllAttributes(function);
		return result;
	}

	@Override
	public boolean run(Module module) {
		try (var builder = new IRBuilder(module)) {
			var folder = new IRFolder(builder);
			var res = module.getFunctions().values().stream().map(func -> processFunction(func, folder))
					.reduce(false, (a, b) -> a || b);
			if (res) QueryManager.getInstance().invalidateAllAttributes(module);
			return res;
		}
	}
}