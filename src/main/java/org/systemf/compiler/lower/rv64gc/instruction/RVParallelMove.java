package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.instruction.PotentialSideEffect;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Pseudo-instruction for parallel virtual register movement
 * <p>
 * Must be placed right before unconditional jump, won't affect the live range of its destinations
 */
public class RVParallelMove extends DummyNonTerminal implements PotentialSideEffect {
	private final Map<Value, Value> moves = new HashMap<>();

	public void removeMove(Value to) {
		if (!moves.containsKey(to)) return;
		moves.get(to).unregisterDependant(this);
		moves.remove(to);
		to.unregisterDependant(this);
	}

	public void addMove(Value to, Value from) {
		if (to instanceof Constant) throw new IllegalArgumentException("Try to move to constant");
		if (moves.containsKey(to)) throw new IllegalStateException("Duplicate move " + ValueUtil.dumpIdentifier(to));
		to.registerDependant(this);
		from.registerDependant(this);
		moves.put(to, from);
	}

	public Map<Value, Value> getMoves() {
		return Collections.unmodifiableMap(moves);
	}

	@Override
	public String toString() {
		return String.format("par_move %s", moves.entrySet().stream()
				.map(entry -> String.format("[%s: %s]", ValueUtil.dumpIdentifier(entry.getKey()),
						ValueUtil.dumpIdentifier(entry.getValue()))).collect(Collectors.joining(", ")));
	}

	@Override
	public Set<ITracked> getDependency() {
		var result = new HashSet<ITracked>(moves.keySet());
		result.addAll(moves.values());
		return result;
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
		for (var entry : moves.entrySet()) {
			var value = entry.getValue();
			if (value != oldValue) continue;
			value.unregisterDependant(this);
			newValue.registerDependant(this);
			entry.setValue((Value) newValue);
		}

		if (oldValue instanceof Value && moves.containsKey(oldValue)) {
			if (newValue instanceof Constant) throw new IllegalArgumentException("Try to move to constant");
			var val = moves.get(oldValue);
			oldValue.unregisterDependant(this);
			moves.remove(oldValue);
			var newValueVal = (Value) newValue;
			if (moves.containsKey(newValueVal)) throw new IllegalStateException("replaceAll overwriting existing dest");
			newValue.registerDependant(this);
			moves.put(newValueVal, val);
		}
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void unregister() {
		moves.forEach((k, v) -> {
			k.unregisterDependant(this);
			v.unregisterDependant(this);
		});
	}
}
