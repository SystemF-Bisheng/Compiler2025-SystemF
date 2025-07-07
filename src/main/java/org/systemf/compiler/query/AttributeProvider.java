package org.systemf.compiler.query;

@FunctionalInterface
public interface AttributeProvider<T, U> {
	U getAttribute(T entity);
}