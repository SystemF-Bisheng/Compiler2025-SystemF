package org.systemf.compiler.semantic.util;

import org.systemf.compiler.util.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Objects;

public class SysYAggregateBuilder<Ty, V, R> {
	private final SysYAggregateHelper<Ty, V, R> aggregateHelper;
	private final Deque<Pair<Ty, ArrayList<R>>> stack = new ArrayDeque<>();
	private R result;

	public SysYAggregateBuilder(SysYAggregateHelper<Ty, V, R> aggregateHelper) {
		this.aggregateHelper = aggregateHelper;
	}

	public void begin(Ty type) {
		stack.push(Pair.of(type, new ArrayList<>()));
		result = null;
	}

	public R end() {
		while (!stack.isEmpty()) foldOnce();
		return result;
	}

	private <T> Ty nextLayerType(Pair<Ty, ArrayList<T>> layer) {
		return aggregateHelper.aggregateType(layer.left, layer.right.size());
	}

	private void unfoldOnce() {
		stack.push(Pair.of(nextLayerType(Objects.requireNonNull(stack.peek())), new ArrayList<>()));
	}

	private void unfoldUntil(Ty type) {
		if (stack.isEmpty()) return;
		while (true) {
			var next = nextLayerType(Objects.requireNonNull(stack.peek()));
			if (aggregateHelper.convertibleTo(type, next)) break;
			if (aggregateHelper.isAggregateAtom(next)) aggregateHelper.onIllegalType(type);
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
		var type = aggregateHelper.typeOf(value);
		unfoldUntil(type);
		var layer = stack.peek();
		if (layer != null) value = aggregateHelper.convertTo(value, type, nextLayerType(layer));
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

	public int beginAggregate() {
		unfoldOnce();
		return stack.size();
	}

	public void endAggregate(int depth) {
		while (stack.size() >= depth) foldOnce();
	}
}