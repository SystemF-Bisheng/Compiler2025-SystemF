package org.systemf.compiler.semantic.util;

import org.systemf.compiler.semantic.type.SysYType;

import java.util.List;

public interface SysYAggregateHelper<V, R> {
	int aggregateCount(SysYType type);

	SysYType aggregateType(SysYType type, int index);

	SysYType typeOf(V value);

	V convertTo(V value, SysYType type);

	R fromValue(V value);

	boolean isAggregateAtom(SysYType type);

	R aggregate(SysYType type, List<R> content);
}