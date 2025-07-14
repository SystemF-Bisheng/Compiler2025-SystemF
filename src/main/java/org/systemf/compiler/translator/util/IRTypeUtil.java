package org.systemf.compiler.translator.util;

import org.antlr.v4.runtime.ParserRuleContext;
import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.parser.SysYParser;
import org.systemf.compiler.semantic.util.SysYTypeUtil;

import java.util.HashMap;

public class IRTypeUtil {
	private final IRBuilder builder;

	public IRTypeUtil(IRBuilder builder) {
		this.builder = builder;
	}

	public Sized typeFromBasicType(SysYParser.BasicTypeContext basicType) {
		if (basicType.INT() != null) return builder.buildI32Type();
		if (basicType.FLOAT() != null) return builder.buildFloatType();
		throw new IllegalArgumentException("Unknown basic type " + basicType.getText());
	}

	public Sized applyArrayPostfix(Sized type, SysYParser.ArrayPostfixContext postfix,
			HashMap<ParserRuleContext, Value> valueMap) {
		for (var post : postfix.arrayPostfixSingle().reversed())
			type = builder.buildArrayType(type,
					Math.toIntExact(SysYTypeUtil.assertArraySize(ValueUtil.getConstantInt(valueMap.get(post)))));
		return type;
	}

	public Sized applyVarDefEntry(Sized type, SysYParser.VarDefEntryContext entry,
			HashMap<ParserRuleContext, Value> valueMap) {
		return applyArrayPostfix(type, entry.arrayPostfix(), valueMap);
	}

	public Type typeFromRetType(SysYParser.RetTypeContext retType) {
		if (retType.VOID() != null) return builder.buildVoidType();
		return typeFromBasicType(retType.basicType());
	}

	public Type typeFromFuncParam(SysYParser.FuncParamContext param, HashMap<ParserRuleContext, Value> valueMap) {
		var basic = typeFromBasicType(param.basicType());
		if (param.incompleteArray() == null) return basic;
		basic = applyArrayPostfix(basic, param.arrayPostfix(), valueMap);
		return builder.buildPointerType(builder.buildUnsizedArrayType(basic));
	}
}