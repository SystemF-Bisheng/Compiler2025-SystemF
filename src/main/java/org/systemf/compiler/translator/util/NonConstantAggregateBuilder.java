package org.systemf.compiler.translator.util;

import org.antlr.v4.runtime.ParserRuleContext;
import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.semantic.type.SysYType;
import org.systemf.compiler.semantic.util.SysYAggregateBuilder;
import org.systemf.compiler.util.Either;
import org.systemf.compiler.util.Pair;

import java.util.function.Consumer;

public class NonConstantAggregateBuilder extends
		SysYAggregateBuilder<SysYType, Pair<ParserRuleContext, Value>, Either<Constant, Consumer<Value>>> {
	public NonConstantAggregateBuilder(IRBuilder builder, SysYValueUtil valueUtil) {
		super(new NonConstantAggregateHelper(builder, valueUtil));
	}
}