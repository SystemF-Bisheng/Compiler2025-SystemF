package org.systemf.compiler.semantic.util;

import java.util.List;

public interface SysYAggregateHelper<Ty, V, R> {
	long aggregateCount(Ty type);

	Ty aggregateType(Ty type, int index);

	Ty typeOf(V value);

	boolean convertibleTo(Ty from, Ty to);

	V convertTo(V value, Ty from, Ty to);

	R fromValue(V value);

	boolean isAggregateAtom(Ty type);

	R aggregate(Ty type, List<R> content);

	void onIllegalType(Ty type);
}