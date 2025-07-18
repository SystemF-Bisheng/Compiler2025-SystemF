package org.systemf.compiler.util;

import java.util.function.BiFunction;

public class PathUnionFindNode<T> {
	public T data;
	private PathUnionFindNode<T> parent;

	public PathUnionFindNode(T data) {
		this.data = data;
	}

	public PathUnionFindNode<T> find(BiFunction<T, T, T> merger) {
		if (parent == null) return this;
		var res = parent.find(merger);
		if (res != parent) {
			data = merger.apply(data, parent.data);
			parent = res;
		}
		return res;
	}

	public void union(PathUnionFindNode<T> to, BiFunction<T, T, T> merger) {
		var top = find(merger);
		var toTop = to.find(merger);
		if (top == toTop) return;
		top.parent = to;
	}
}