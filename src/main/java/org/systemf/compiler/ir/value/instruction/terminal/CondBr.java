package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;

public class CondBr extends DummyTerminal {
	public final Value cond;
	public final BasicBlock trueTarget, falseTarget;

	public CondBr(Value cond, BasicBlock trueTarget, BasicBlock falseTarget) {
		this.cond = cond;
		this.trueTarget = trueTarget;
		this.falseTarget = falseTarget;
	}

	@Override
	public String toString() {
		return String.format("cond_br %%%s, %%%s, %%%s", ValueUtil.getValueName(cond), trueTarget.getName(),
				falseTarget.getName());
	}
}