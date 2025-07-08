package org.systemf.compiler.util;

public class Pair<T, U> {
	public T left;
	public U right;

	public Pair(T left, U right) {
		this.left = left;
		this.right = right;
	}

	public static <A, B> Pair<A, B> of(A a, B b) {
		return new Pair<>(a, b);
	}

	public T getLeft() {
		return left;
	}

	public U getRight() {
		return right;
	}
}