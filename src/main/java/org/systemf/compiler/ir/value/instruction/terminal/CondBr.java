package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Collections;
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
	public Set<Value> getDependency() {
		return Collections.singleton(cond);
	}

	@Override
	public void replaceAll(Value oldValue, Value newValue) {
		if (cond == oldValue) setCondition(newValue);
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void unregister() {
		if (cond != null) cond.unregisterDependant(this);
	}

	public BasicBlock getTrueTarget() {
		return trueTarget;
	}

	public void setTrueTarget(BasicBlock trueTarget) {
		this.trueTarget = trueTarget;
	}

	public BasicBlock getFalseTarget() {
		return falseTarget;
	}

	public void setFalseTarget(BasicBlock falseTarget) {
		this.falseTarget = falseTarget;
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