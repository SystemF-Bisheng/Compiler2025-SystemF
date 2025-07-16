package org.systemf.compiler.util;

public record Pair<T, U>(T left, U right) {
	public static <A, B> Pair<A, B> of(A a, B b) {
		return new Pair<>(a, b);
	}

	public <L> Pair<L, U> withLeft(L left) {
		return Pair.of(left, right);
	}

	public <R> Pair<T, R> withRight(R right) {
		return Pair.of(left, right);
	}
}