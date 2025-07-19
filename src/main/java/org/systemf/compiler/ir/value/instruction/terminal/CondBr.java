package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CondBr extends DummyTerminal {
	private Value cond;
	private BasicBlock trueTarget;
	private BasicBlock falseTarget;

	public CondBr(Value cond, BasicBlock trueTarget, BasicBlock falseTarget) {
		setCondition(cond);
		setTrueTarget(trueTarget);
		setFalseTarget(falseTarget);
	}

	@Override
	public String toString() {
		return String.format("cond_br %s, %s, %s", ValueUtil.dumpIdentifier(cond), trueTarget.getName(),
				falseTarget.getName());
	}

	@Override
	public Set<ITracked> getDependency() {
		return new HashSet<>(List.of(cond, trueTarget, falseTarget));
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
		if (trueTarget == oldValue) setTrueTarget((BasicBlock) newValue);
		if (falseTarget == oldValue) setFalseTarget((BasicBlock) newValue);
		if (cond == oldValue) setCondition((Value) newValue);
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void unregister() {
		if (cond != null) cond.unregisterDependant(this);
		if (trueTarget != null) trueTarget.unregisterDependant(this);
		if (falseTarget != null) falseTarget.unregisterDependant(this);
	}

	public BasicBlock getTrueTarget() {
		return trueTarget;
	}

	public void setTrueTarget(BasicBlock trueTarget) {
		if (this.trueTarget != null) this.trueTarget.unregisterDependant(this);
		this.trueTarget = trueTarget;
		trueTarget.registerDependant(this);
	}

	public BasicBlock getFalseTarget() {
		return falseTarget;
	}

	public void setFalseTarget(BasicBlock falseTarget) {
		if (this.falseTarget != null) this.falseTarget.unregisterDependant(this);
		this.falseTarget = falseTarget;
		falseTarget.registerDependant(this);
	}

	public Value getCondition() {
		return cond;
	}

	public void setCondition(Value cond) {
		TypeUtil.assertConvertible(cond.getType(), I32.INSTANCE, "Illegal condition");
		if (this.cond != null) this.cond.unregisterDependant(this);
		this.cond = cond;
		cond.registerDependant(this);
	}
}