package org.systemf.compiler.util;

import java.util.Objects;
import java.util.function.Function;

public class Either<A, B> {
	private final A a;
	private final B b;

	private Either(A a) {
		this.a = a;
		this.b = null;
	}

	private Either(B b, int ignored) {
		this.a = null;
		this.b = b;
	}

	public static <A, B> Either<A, B> ofA(A a) {
		return new Either<>(a);
	}

	public static <A, B> Either<A, B> ofB(B b) {
		return new Either<>(b, 0);
	}

	@SuppressWarnings("unchecked")
	public <R> Either<R, B> mapA(Function<A, R> f) {
		if (a == null) return (Either<R, B>) this;
		return new Either<>(f.apply(a));
	}

	@SuppressWarnings("unchecked")
	public <R> Either<A, R> mapB(Function<B, R> f) {
		if (b == null) return (Either<A, R>) this;
		return new Either<>(f.apply(b), 0);
	}

	public boolean isA() {
		return a != null;
	}

	public boolean isB() {
		return b != null;
	}

	public A getA() {
		return Objects.requireNonNull(a);
	}

	public B getB() {
		return Objects.requireNonNull(b);
	}
}