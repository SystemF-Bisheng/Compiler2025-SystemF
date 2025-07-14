package org.systemf.compiler.util;

import java.util.HashMap;
import java.util.NoSuchElementException;

public class Context<T> {
	private ContextLayer<T> current = null;

	public void push() {
		current = new ContextLayer<>(current);
	}

	public void pop() {
		if (current == null) throw new IllegalStateException("Context underflow");
		current = current.parent;
	}

	public T get(String key) {
		if (current == null) throw new IllegalStateException("Context underflow");
		return current.get(key);
	}

	public void define(String key, T value) {
		if (current == null) throw new IllegalStateException("Context underflow");
		current.define(key, value);
	}

	public boolean contains(String key) {
		if (current == null) return false;
		return current.contains(key);
	}

	public ContextLayer<T> top() {
		return current;
	}

	public boolean empty() {
		return current == null;
	}

	public static class ContextLayer<T> {
		public final ContextLayer<T> parent;
		public final HashMap<String, T> content = new HashMap<>();

		public ContextLayer(ContextLayer<T> parent) {
			this.parent = parent;
		}

		public T get(String key) {
			if (content.containsKey(key)) return content.get(key);
			if (parent == null) throw new NoSuchElementException(key);
			return parent.get(key);
		}

		public void define(String key, T value) {
			if (content.containsKey(key)) throw new RuntimeException("Duplicate key: " + key);
			content.put(key, value);
		}

		public boolean contains(String key) {
			if (content.containsKey(key)) return true;
			if (parent == null) return false;
			return parent.contains(key);
		}

		public boolean containsLocal(String key) {
			return content.containsKey(key);
		}
	}
}