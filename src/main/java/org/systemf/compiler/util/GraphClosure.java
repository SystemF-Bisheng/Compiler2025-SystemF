package org.systemf.compiler.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

public class GraphClosure<N, T> {
	private final HashMap<N, HashSet<T>> data = new HashMap<>();
	private final HashMap<N, HashSet<N>> edges = new HashMap<>();
	private final HashMap<N, Consumer<T>> reaction = new HashMap<>();

	public void addData(N n, T d) {
		var dataSet = data.computeIfAbsent(n, _ -> new HashSet<>());
		if (dataSet.contains(d)) return;
		dataSet.add(d);
		var edgesSet = edges.computeIfAbsent(n, _ -> new HashSet<>());
		for (var edge : edgesSet) addData(edge, d);
		reaction.getOrDefault(n, _ -> {
		}).accept(d);
	}

	public void addEdge(N from, N to) {
		var edgesSet = edges.computeIfAbsent(from, _ -> new HashSet<>());
		if (edgesSet.contains(to)) return;
		edgesSet.add(to);
		var dataSet = data.computeIfAbsent(from, _ -> new HashSet<>());
		for (var d : dataSet) addData(to, d);
	}

	public void addReaction(N n, Consumer<T> r) {
		reaction.compute(n, (_, v) -> v == null ? r : v.andThen(r));
		var dataSet = data.computeIfAbsent(n, _ -> new HashSet<>());
		for (var d : dataSet) r.accept(d);
	}

	public Map<N, HashSet<T>> getData() {
		return Collections.unmodifiableMap(data);
	}
}