package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.value.Value;

public class RVBranchEq extends RVCompBranch {
	public RVBranchEq(Value x, Value y, BasicBlock trueTarget, BasicBlock falseTarget) {
		super(x, y, trueTarget, falseTarget);
	}

	@Override
	protected String operatorName() {
		return "beq";
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
