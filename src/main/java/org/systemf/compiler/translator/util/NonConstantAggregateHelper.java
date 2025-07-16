package org.systemf.compiler.translator.util;

import org.antlr.v4.runtime.ParserRuleContext;
import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.semantic.type.SysYArray;
import org.systemf.compiler.semantic.type.SysYFloat;
import org.systemf.compiler.semantic.type.SysYInt;
import org.systemf.compiler.semantic.type.SysYType;
import org.systemf.compiler.util.Either;
import org.systemf.compiler.util.Pair;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NonConstantAggregateHelper extends SimpleIRAggregateHelper<Either<Constant, Consumer<Value>>> {
	private final IRBuilder builder;
	private final SysYValueUtil valueUtil;

	public NonConstantAggregateHelper(IRBuilder builder, SysYValueUtil valueUtil) {
		this.builder = builder;
		this.valueUtil = valueUtil;
	}

	@Override
	public Either<Constant, Consumer<Value>> aggregateDefault(SysYType type) {
		return Either.ofA(valueUtil.aggregateDefaultValue(type));
	}

	@Override
	public Pair<ParserRuleContext, Value> convertTo(Pair<ParserRuleContext, Value> value, SysYType from, SysYType to) {
		return value.withRight(valueUtil.convertTo(value.right(), from, to));
	}

	@Override
	public Either<Constant, Consumer<Value>> fromValue(Pair<ParserRuleContext, Value> value) {
		var src = value.right();
		if (src instanceof Constant c) return Either.ofA(c);
		return Either.ofB(v -> builder.buildStore(src, v));
	}

	@Override
	public Either<Constant, Consumer<Value>> aggregate(SysYType type, List<Either<Constant, Consumer<Value>>> content) {
		padContent(type, content);

		if (type == SysYInt.INT) return content.getFirst();
		if (type == SysYFloat.FLOAT) return content.getFirst();

		if (content.stream().allMatch(Either::isA)) return Either.ofA(
				valueUtil.aggregateConstant(type, content.stream().map(Either::getA).collect(Collectors.toList())));

		if (type instanceof SysYArray(_, long length)) {
			var iter = content.iterator();
			Consumer<Value> res = null;
			for (int i = 0; i < length; ++i) {
				var entry = iter.next();
				var indexI = builder.buildConstantInt(i);
				var initializer = valueUtil.toConsumer(entry);
				Consumer<Value> entryCon = v -> initializer.accept(builder.buildGetPtr(v, indexI, "initArr"));

				if (res == null) res = entryCon;
				else res = res.andThen(entryCon);
			}
			return Either.ofB(res);
		}
		throw new IllegalArgumentException("Invalid aggregate type " + type);
	}
}