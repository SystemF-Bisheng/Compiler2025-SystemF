package org.systemf.compiler.util;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CollectionUtil {
	public static <T> SequencedSet<T> singletonSequencedSet(T x) {
		return new SingletonSequencedSet<>(x);
	}

	static <E> Iterator<E> singletonIterator(final E e) {
		return new Iterator<>() {
			private boolean hasNext = true;

			public boolean hasNext() {
				return this.hasNext;
			}

			public E next() {
				if (this.hasNext) {
					this.hasNext = false;
					return e;
				} else throw new NoSuchElementException();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			public void forEachRemaining(Consumer<? super E> action) {
				Objects.requireNonNull(action);
				if (this.hasNext) {
					this.hasNext = false;
					action.accept(e);
				}
			}
		};
	}

	static <T> Spliterator<T> singletonSpliterator(final T element) {
		return new Spliterator<>() {
			long est = 1L;

			public Spliterator<T> trySplit() {
				return null;
			}

			public boolean tryAdvance(Consumer<? super T> consumer) {
				Objects.requireNonNull(consumer);
				if (this.est > 0L) {
					--this.est;
					consumer.accept(element);
					return true;
				} else return false;
			}

			public void forEachRemaining(Consumer<? super T> consumer) {
				this.tryAdvance(consumer);
			}

			public long estimateSize() {
				return this.est;
			}

			public int characteristics() {
				int value = element != null ? Spliterator.NONNULL : 0;
				return value | Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT |
				       Spliterator.ORDERED;
			}
		};
	}

	private static class SingletonSequencedSet<E> extends AbstractSet<E> implements Serializable, SequencedSet<E> {
		private final E element;

		SingletonSequencedSet(E e) {
			this.element = e;
		}

		public Iterator<E> iterator() {
			return singletonIterator(this.element);
		}

		public int size() {
			return 1;
		}

		public boolean contains(Object o) {
			return Objects.equals(o, this.element);
		}

		public void forEach(Consumer<? super E> action) {
			action.accept(this.element);
		}

		public Spliterator<E> spliterator() {
			return singletonSpliterator(this.element);
		}

		public boolean removeIf(Predicate<? super E> filter) {
			throw new UnsupportedOperationException();
		}

		public int hashCode() {
			return Objects.hashCode(this.element);
		}

		@Override
		public SequencedSet<E> reversed() {
			return this;
		}
	}
}
