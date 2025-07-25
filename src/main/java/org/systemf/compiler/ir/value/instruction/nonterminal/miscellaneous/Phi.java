package org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.PotentialBlockSensitive;
import org.systemf.compiler.ir.value.instruction.PotentialNonRepeatable;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.*;

public class Phi extends DummyValueNonTerminal implements PotentialNonRepeatable, PotentialBlockSensitive {
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
	public Set<ITracked> getDependency() {
		var res = new HashSet<ITracked>();
		res.addAll(incoming.keySet());
		res.addAll(incoming.values());
		return res;
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
		for (var entry : incoming.entrySet()) {
			var value = entry.getValue();
			if (value == oldValue) {
				var val = (Value) newValue;
				checkIncoming(val);
				value.unregisterDependant(this);
				entry.setValue(val);
				newValue.registerDependant(this);
			}
		}
		if (oldValue instanceof BasicBlock && incoming.containsKey(oldValue)) {
			var val = incoming.get(oldValue);
			incoming.remove(oldValue);
			oldValue.unregisterDependant(this);
			incoming.put((BasicBlock) newValue, val);
			newValue.registerDependant(this);
		}
	}

	@Override
	public void unregister() {
		incoming.forEach((block, value) -> {
			block.unregisterDependant(this);
			value.unregisterDependant(this);
		});
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
		this.incoming.forEach((block, value) -> {
			block.unregisterDependant(this);
			value.unregisterDependant(this);
		});
		this.incoming = new HashMap<>(incoming);
		incoming.forEach((block, value) -> {
			block.registerDependant(this);
			value.registerDependant(this);
		});
	}

	public void removeIncoming(BasicBlock block) {
		if (!incoming.containsKey(block)) return;
		block.unregisterDependant(this);
		incoming.get(block).unregisterDependant(this);
		incoming.remove(block);
	}

	private void checkIncoming(Value value) {
		TypeUtil.assertConvertible(value.getType(), type, "Illegal incoming");
	}

	public void addIncoming(BasicBlock block, Value value) {
		checkIncoming(value);
		if (incoming.containsKey(block)) throw new IllegalArgumentException("Duplicate incoming block");
		incoming.put(block, value);
		block.registerDependant(this);
		value.registerDependant(this);
	}

	@Override
	public boolean contentEqual(Value other) {
		if (!(other instanceof Phi phi)) return false;
		if (incoming.size() != phi.incoming.size()) return false;
		for (var entry : incoming.entrySet()) {
			var key = entry.getKey();
			var value = entry.getValue();
			if (!phi.incoming.containsKey(key)) return false;
			if (!ValueUtil.trivialInterchangeable(phi.incoming.get(key), value)) return false;
		}
		return true;
	}
}