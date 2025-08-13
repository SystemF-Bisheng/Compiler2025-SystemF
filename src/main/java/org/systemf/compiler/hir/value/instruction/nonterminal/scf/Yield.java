package org.systemf.compiler.hir.value.instruction.nonterminal.scf;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.terminal.DummyTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.stream.Collectors;

public class Yield extends DummyTerminal {
	private Value[] yieldValues;

	public Yield(Value... yieldValues) {
		this.yieldValues = yieldValues;
	}

	public Value[] getYieldValues() {
		return yieldValues;
	}

	public void setYieldValues(Value... yieldValues) {
		if (this.yieldValues.length != yieldValues.length) {
			throw new UnsupportedOperationException(String.format(
					"set yield values with different length `%s` vs. `%s`",
					this.yieldValues.length, yieldValues.length
			));
		}
		for (int i = 0; i < yieldValues.length; i++) {
			if (!this.yieldValues[i].getType().equals(yieldValues[i].getType())) {
				throw new UnsupportedOperationException(String.format(
						"set yield value with different type `%s` vs. `%s`",
						this.yieldValues[i].getType(), yieldValues[i].getType()
				));
			}
		}
		this.yieldValues = yieldValues;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("yield ");
		for (var yieldValue : yieldValues) {
			builder.append(ValueUtil.dumpIdentifier(yieldValue)).append(", ");
		}
		if (yieldValues.length > 0) {
			builder.delete(builder.length() - 2, builder.length());
		}
		return builder.toString();
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
		for (int i = 0; i < yieldValues.length; i++) {
			if (yieldValues[i] == oldValue) {
				yieldValues[i] = (Value) newValue;
			}
		}
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void unregister() {
		for (var yieldValue : yieldValues) {
			if (yieldValue != null) {
				yieldValue.unregisterDependant(this);
			}
		}
	}

	@Override
	public SequencedSet<ITracked> getDependency() {
		return Arrays.stream(yieldValues).collect(Collectors.toCollection(LinkedHashSet::new));
	}
}
