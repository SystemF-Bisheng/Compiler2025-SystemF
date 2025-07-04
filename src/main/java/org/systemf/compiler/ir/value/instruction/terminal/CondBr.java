package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.value.Value;

public class CondBr extends DummyTerminal {
	public final Value cond;
	public final BasicBlock trueTarget, falseTarget;

	public CondBr(Value cond, BasicBlock trueTarget, BasicBlock falseTarget) {
		this.cond = cond;
		this.trueTarget = trueTarget;
		this.falseTarget = falseTarget;
	}
}