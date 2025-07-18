package org.systemf.compiler.util;

import java.util.*;

public class Tree<T> {
	private final Map<T, List<T>> children = new HashMap<>();
	private final Map<T, T> parent = new HashMap<>();
	private final Map<T, Integer> dfn = new HashMap<>();
	private final Map<T, Pair<Integer, Integer>> dfnRange = new HashMap<>();
	private final Map<T, List<T>> preparedParent = new HashMap<>();
	private T root;
	private int dfnCnt = 0;

	public Tree(List<Pair<T, T>> nodes) {
		for (var pair : nodes) {
			if (pair.left() == null) throw new IllegalArgumentException();
			children.put(pair.left(), new ArrayList<>());
			preparedParent.put(pair.left(), new ArrayList<>());
		}
		for (var pair : nodes) {
			if (pair.right() == null) {
				if (root == null) root = pair.left();
				else throw new IllegalArgumentException();
			} else {
				parent.put(pair.left(), pair.right());
				children.get(pair.right()).add(pair.left());
			}
		}
		if (root == null) throw new IllegalArgumentException();
		prepare(root);
	}

	private void prepare(T cur) {
		var curDfn = dfnCnt++;
		dfn.put(cur, curDfn);
		for (var child : children.get(cur)) prepare(child);
		dfnRange.put(cur, Pair.of(curDfn, dfnCnt - 1));

		if (parent.containsKey(cur)) {
			var prepared = preparedParent.get(cur);
			var index = 0;
			for (var father = parent.get(cur); ; ++index) {
				prepared.add(father);
				var faPrepared = preparedParent.get(father);
				if (faPrepared.size() <= index) break;
				father = faPrepared.get(index);
			}
		}
	}

	public boolean subtree(T parent, T child) {
		var range = dfnRange.get(parent);
		var childDfn = dfn.get(child);
		return range.left() <= childDfn && childDfn <= range.right();
	}

	public T lca(T a, T b) {
		if (subtree(a, b)) return a;
		var aPrepared = preparedParent.get(a);
		var nxtA = aPrepared.getFirst();
		for (var par : aPrepared) {
			if (subtree(par, b)) break;
			nxtA = par;
		}
		return lca(nxtA, b);
	}

	public List<T> getChildren(T node) {
		return Collections.unmodifiableList(children.get(node));
	}

	public T getRoot() {
		return root;
	}

	public T getParent(T node) {
		return parent.get(node);
	}

	public int getDfn(T node) {
		return dfn.get(node);
	}

	public Pair<Integer, Integer> getDfnRange(T node) {
		return dfnRange.get(node);
	}
}