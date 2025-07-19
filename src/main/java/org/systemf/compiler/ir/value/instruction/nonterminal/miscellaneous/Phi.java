package org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.*;

public class Phi extends DummyValueNonTerminal {
	private Map<BasicBlock, Value> incoming = new HashMap<>();

	public Phi(Type type, String name) {
		super(type, name);
	}

	@Override
	public String dumpInstructionBody() {
		StringBuilder sb = new StringBuilder();
		boolean nonFirst = false;
		for (var entry : incoming.entrySet()) {
			if (nonFirst) sb.append(", ");
			nonFirst = true;
			sb.append("[ ");
			sb.append(ValueUtil.dumpIdentifier(entry.getValue())).append(": ").append(entry.getKey().getName());
			sb.append(" ]");
		}
		return sb.toString();
	}

	@Override
	public Set<Value> getDependency() {
		return new HashSet<>(incoming.values());
	}

	@Override
	public void replaceAll(Value oldValue, Value newValue) {
		checkIncoming(newValue);
		for (var entry : incoming.entrySet()) {
			var value = entry.getValue();
			if (value == oldValue) {
				value.unregisterDependant(this);
				entry.setValue(newValue);
				newValue.registerDependant(this);
			}
		}
	}

	@Override
	public void replaceAll(BasicBlock oldBlock, BasicBlock newBlock) {
		if (incoming.containsKey(oldBlock)) {
			var value = incoming.get(oldBlock);
			incoming.remove(oldBlock);
			incoming.put(newBlock, value);
		}
	}

	@Override
	public void unregister() {
		incoming.values().forEach(v -> v.unregisterDependant(this));
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	public Map<BasicBlock, Value> getIncoming() {
		return Collections.unmodifiableMap(incoming);
	}

	public void setIncoming(Map<BasicBlock, Value> incoming) {
		incoming.values().forEach(this::checkIncoming);
		this.incoming.values().forEach(v -> v.unregisterDependant(this));
		this.incoming = new HashMap<>(incoming);
		this.incoming.values().forEach(v -> v.registerDependant(this));
	}

	private void checkIncoming(Value value) {
		TypeUtil.assertConvertible(value.getType(), type, "Illegal incoming");
	}

	public void addIncoming(BasicBlock block, Value value) {
		checkIncoming(value);
		if (incoming.containsKey(block)) throw new IllegalArgumentException("Duplicate incoming block");
		incoming.put(block, value);
		value.registerDependant(this);
	}
}