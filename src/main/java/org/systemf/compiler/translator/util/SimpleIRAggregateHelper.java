package org.systemf.compiler.translator.util;

import org.antlr.v4.runtime.ParserRuleContext;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.semantic.type.SysYArray;
import org.systemf.compiler.semantic.type.SysYFloat;
import org.systemf.compiler.semantic.type.SysYInt;
import org.systemf.compiler.semantic.type.SysYType;
import org.systemf.compiler.semantic.util.SysYAggregateHelper;
import org.systemf.compiler.semantic.value.ValueAndType;
import org.systemf.compiler.util.Pair;

import java.util.List;

public abstract class SimpleIRAggregateHelper<R> implements
		SysYAggregateHelper<SysYType, Pair<ParserRuleContext, Value>, R> {
	private static final QueryManager query = QueryManager.getInstance();

	@Override
	public long aggregateCount(SysYType type) {
		if (type == SysYInt.INT) return 1;
		if (type == SysYFloat.FLOAT) return 1;
		if (type instanceof SysYArray arr) return arr.length();
		throw new IllegalArgumentException("Invalid aggregate type " + type);
	}

	@Override
	public SysYType aggregateType(SysYType type, int index) {
		if (index >= aggregateCount(type)) throw new IndexOutOfBoundsException("Illegal aggregate index " + index);
		if (type == SysYInt.INT) return SysYInt.INT;
		if (type == SysYFloat.FLOAT) return SysYFloat.FLOAT;
		if (type instanceof SysYArray arr) return arr.element();
		throw new IllegalArgumentException("Invalid aggregate type " + type);
	}

	@Override
	public SysYType typeOf(Pair<ParserRuleContext, Value> value) {
		return query.getAttribute(value.left, ValueAndType.class).type();
	}

	@Override
	public boolean convertibleTo(SysYType from, SysYType to) {
		return from.convertibleTo(to);
	}

	@Override
	public boolean isAggregateAtom(SysYType type) {
		if (type == SysYInt.INT) return true;
		return type == SysYFloat.FLOAT;
	}

	@Override
	public void onIllegalType(SysYType type) {
		throw new IllegalArgumentException("Illegal value type " + type);
	}

	public abstract R aggregateDefault(SysYType type);

	protected void padContent(SysYType type, List<R> content) {
		var needed = aggregateCount(type);
		if (content.size() > needed) throw new IllegalArgumentException("Got more content than expected");
		while (content.size() < needed) content.add(aggregateDefault(aggregateType(type, content.size())));
	}
}