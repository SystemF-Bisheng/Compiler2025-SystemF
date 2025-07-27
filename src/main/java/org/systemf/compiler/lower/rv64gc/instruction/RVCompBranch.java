package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.terminal.DummyTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class RVCompBranch extends DummyTerminal {
	private Value x;
	private Value y;
	private BasicBlock trueTarget;
	private BasicBlock falseTarget;

	protected RVCompBranch(Value x, Value y, BasicBlock trueTarget, BasicBlock falseTarget) {
		setX(x);
		setY(y);
		setTrueTarget(trueTarget);
		setFalseTarget(falseTarget);
	}

	protected abstract String operatorName();

	@Override
	public String toString() {
		return String.format("%s %s, %s; %s, %s", operatorName(), ValueUtil.dumpIdentifier(x),
				ValueUtil.dumpIdentifier(y), trueTarget.getName(), falseTarget.getName());
	}

	@Override
	public Set<ITracked> getDependency() {
		return new HashSet<>(List.of(x, y, trueTarget, falseTarget));
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
		if (trueTarget == oldValue) setTrueTarget((BasicBlock) newValue);
		if (falseTarget == oldValue) setFalseTarget((BasicBlock) newValue);
		if (x == oldValue) setX((Value) newValue);
		if (y == oldValue) setY((Value) newValue);
	}

	@Override
	public void unregister() {
		if (x != null) x.unregisterDependant(this);
		if (y != null) y.unregisterDependant(this);
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

	public Value getX() {
		return x;
	}

	public void setX(Value x) {
		if (this.x != null) this.x.unregisterDependant(this);
		this.x = x;
		x.registerDependant(this);
	}

	public Value getY() {
		return y;
	}

	public void setY(Value y) {
		if (this.y != null) this.y.unregisterDependant(this);
		this.y = y;
		y.registerDependant(this);
	}
}
