package org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Phi extends DummyValueNonTerminal {
	private List<Pair<BasicBlock, Value>> incoming = new ArrayList<>();

	public Phi(Type type, String name) {
		super(type, name);
	}

	@Override
	public String dumpInstructionBody() {
		StringBuilder sb = new StringBuilder();
		boolean nonFirst = false;
		for (var pair : incoming) {
			if (nonFirst) sb.append(", ");
			nonFirst = true;
			sb.append("[ ");
			sb.append(ValueUtil.dumpIdentifier(pair.right())).append(", ").append(pair.left().getName());
			sb.append(" ]");
		}
		return sb.toString();
	}

	@Override
	public Set<Value> getDependency() {
		return incoming.stream().map(Pair::right).collect(Collectors.toSet());
	}

	@Override
	public void replaceAll(Value oldValue, Value newValue) {
		checkIncoming(newValue);
		for (var iter = incoming.listIterator(); iter.hasNext(); ) {
			var pair = iter.next();
			var value = pair.right();
			if (value == oldValue) {
				value.unregisterDependant(this);
				iter.set(pair.withRight(newValue));
				newValue.registerDependant(this);
			}
		}
	}

	@Override
	public void unregister() {
		incoming.stream().map(Pair::right).forEach(v -> v.unregisterDependant(this));
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	public List<Pair<BasicBlock, Value>> getIncoming() {
		return Collections.unmodifiableList(incoming);
	}

	public void setIncoming(List<Pair<BasicBlock, Value>> incoming) {
		incoming.stream().map(Pair::right).forEach(this::checkIncoming);
		this.incoming.stream().map(Pair::right).forEach(v -> v.unregisterDependant(this));
		this.incoming = new ArrayList<>(incoming);
		this.incoming.stream().map(Pair::right).forEach(v -> v.registerDependant(this));
	}

	private void checkIncoming(Value value) {
		TypeUtil.assertConvertible(value.getType(), type, "Illegal incoming");
	}

	public void addIncoming(BasicBlock block, Value value) {
		checkIncoming(value);
		incoming.add(Pair.of(block, value));
		value.registerDependant(this);
	}
}