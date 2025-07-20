package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.CFGAnalysisResult;
import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.IRFolder;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;
import org.systemf.compiler.query.QueryManager;

import java.util.HashSet;

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

		if (result) {
			var query = QueryManager.getInstance();
			query.invalidateAllAttributes(function);
			var cfg = query.getAttribute(function, CFGAnalysisResult.class);
			for (var block : function.getBlocks()) {
				var preds = cfg.predecessors(block);
				block.instructions.stream().takeWhile(inst -> inst instanceof Phi).map(inst -> (Phi) inst)
						.forEach(phi -> {
							var tmp = new HashSet<>(phi.getIncoming().keySet());
							for (var income : tmp) if (!preds.contains(income)) phi.removeIncoming(income);
						});
			}
		}

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