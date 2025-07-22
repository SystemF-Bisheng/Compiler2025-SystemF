package org.systemf.compiler.translator.util;

import org.antlr.v4.runtime.ParserRuleContext;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.semantic.type.SysYType;
import org.systemf.compiler.semantic.util.SysYAggregateBuilder;
import org.systemf.compiler.util.Pair;

public class ConstantAggregateBuilder extends SysYAggregateBuilder<SysYType, Pair<ParserRuleContext, Value>, Constant> {
	public ConstantAggregateBuilder(SysYValueUtil valueUtil) {
		super(new ConstantAggregateHelper(valueUtil));
	}
}