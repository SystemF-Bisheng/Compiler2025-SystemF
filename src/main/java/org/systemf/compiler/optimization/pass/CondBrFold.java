package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.IRFolder;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;
import org.systemf.compiler.query.QueryManager;

public enum CondBrFold implements OptPass {
	INSTANCE;

	private boolean processFunction(Function function, IRFolder folder) {
		boolean result = false;

		for (var block : function.getBlocks()) {
			var terminator = block.getTerminator();
			var folded = terminator.accept(folder);
			if (folded.isPresent()) {
				result = true;
				var newTerm = (Terminal) folded.get();
				terminator.unregister();
				block.instructions.removeLast();
				block.instructions.addLast(newTerm);
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