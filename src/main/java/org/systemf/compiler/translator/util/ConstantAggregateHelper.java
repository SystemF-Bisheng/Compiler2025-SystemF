package org.systemf.compiler.translator.util;

import org.antlr.v4.runtime.ParserRuleContext;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.semantic.type.SysYType;
import org.systemf.compiler.util.Pair;

import java.util.List;

public class ConstantAggregateHelper extends SimpleIRAggregateHelper<Constant> {
	private final SysYValueUtil valueUtil;

	public ConstantAggregateHelper(SysYValueUtil valueUtil) {
		this.valueUtil = valueUtil;
	}

	@Override
	public Constant aggregateDefault(SysYType type) {
		return valueUtil.aggregateDefaultValue(type);
	}

	@Override
	public Pair<ParserRuleContext, Value> convertTo(Pair<ParserRuleContext, Value> value, SysYType from, SysYType to) {
		return value.withRight(valueUtil.constConvertTo(ValueUtil.assertConstant(value.right()), from, to));
	}

	@Override
	public Constant fromValue(Pair<ParserRuleContext, Value> value) {
		return ValueUtil.assertConstant(value.right());
	}

	@Override
	public Constant aggregate(SysYType type, List<Constant> content) {
		if (content.isEmpty()) return aggregateDefault(type);
		padContent(type, content);
		return valueUtil.aggregateConstant(type, content);
	}
}