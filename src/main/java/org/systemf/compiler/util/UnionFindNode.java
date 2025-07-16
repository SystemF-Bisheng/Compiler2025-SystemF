package org.systemf.compiler.util;

import java.util.function.BiFunction;

public class UnionFindNode<T> {
	public T data;
	private int size = 1;
	private UnionFindNode<T> parent;

	public UnionFindNode(T data) {
		this.data = data;
	}

	public UnionFindNode<T> find() {
		if (parent == null) return this;
		var res = parent.find();
		parent = res;
		return res;
	}

	public void union(UnionFindNode<T> other, BiFunction<T, T, T> merger) {
		var thisRoot = find();
		var otherRoot = other.find();
		if (thisRoot == otherRoot) return;

		if (thisRoot.size < otherRoot.size) {
			var tmp = otherRoot;
			otherRoot = thisRoot;
			thisRoot = tmp;
		}

		thisRoot.size += otherRoot.size;
		otherRoot.size = 0;
		otherRoot.parent = thisRoot;
		thisRoot.data = merger.apply(thisRoot.data, otherRoot.data);
		otherRoot.data = null;
	}
}