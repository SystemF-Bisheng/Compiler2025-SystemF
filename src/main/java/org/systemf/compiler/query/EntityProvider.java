package org.systemf.compiler.query;

@FunctionalInterface
public interface EntityProvider<T> {
	T produce();
}