package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.constant.Undefined;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.optimization.pass.util.ChainedRemoveHelper;
import org.systemf.compiler.query.QueryManager;

import java.util.HashSet;

/**
 * Remove instructions that directly use undefined
 * <p>
 * Depend on: No
 * <p>
 * Applicable to: IR, RV64GC
 */
public enum RemoveUndefined implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		var unusedInst = new HashSet<Instruction>();
		module.getFunctions().values().stream().flatMap(Function::allInstructions)
				.filter(inst -> !(inst instanceof Phi))
				.filter(inst -> inst.getDependency().stream().anyMatch(dep -> dep instanceof Undefined))
				.forEach(inst -> ChainedRemoveHelper.markUnused(inst, unusedInst));

		return ChainedRemoveHelper.cleanModule(module, unusedInst, QueryManager.getInstance());
	}
}
