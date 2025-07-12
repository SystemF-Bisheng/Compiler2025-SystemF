package org.systemf.compiler.semantic.util;

import org.systemf.compiler.semantic.type.SysYType;
import org.systemf.compiler.util.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Objects;

public class SysYAggregateBuilder<V, R> {
	private final SysYAggregateHelper<V, R> aggregateHelper;
	private final Deque<Pair<SysYType, ArrayList<R>>> stack = new ArrayDeque<>();
	private int depth;
	private R result;

	public SysYAggregateBuilder(SysYAggregateHelper<V, R> aggregateHelper) {
		this.aggregateHelper = aggregateHelper;
	}

	public void begin(SysYType type) {
		stack.push(new Pair<>(type, new ArrayList<>()));
		depth = 1;
		result = null;
	}

	public R end() {
		depth = 0;
		fold();
		return result;
	}

	private <T> SysYType nextLayerType(Pair<SysYType, ArrayList<T>> layer) {
		return aggregateHelper.aggregateType(layer.left, layer.right.size());
	}

	private void unfoldOnce() {
		stack.push(new Pair<>(nextLayerType(Objects.requireNonNull(stack.peek())), new ArrayList<>()));
	}

	private void unfoldUntil(SysYType type) {
		if (stack.isEmpty()) return;
		while (true) {
			var next = nextLayerType(Objects.requireNonNull(stack.peek()));
			if (type.convertibleTo(next)) break;
			if (aggregateHelper.isAggregateAtom(next)) throw new IllegalArgumentException("Unexpected type: " + type);
			unfoldOnce();
		}
	}

	private R foldHead() {
		var layer = stack.pop();
		return aggregateHelper.aggregate(layer.left, layer.right);
	}

	private void foldOnce() {
		addResult(foldHead());
	}

	public void addValue(V value) {
		unfoldUntil(aggregateHelper.typeOf(value));
		var layer = stack.peek();
		if (layer != null) value = aggregateHelper.convertTo(value, nextLayerType(layer));
		addResult(aggregateHelper.fromValue(value));
	}

	public void addResult(R layerResult) {
		if (stack.isEmpty()) {
			result = layerResult;
			return;
		}
		var layer = Objects.requireNonNull(stack.peek());
		layer.right.add(layerResult);
		if (aggregateHelper.aggregateCount(layer.left) == layer.right.size()) foldOnce();
	}

	private void fold() {
		while (stack.size() > depth) foldOnce();
	}

	public void beginAggregate() {
		fold();
		++depth;
		unfoldOnce();
	}

	public void endAggregate() {
		--depth;
		fold();
	}
}