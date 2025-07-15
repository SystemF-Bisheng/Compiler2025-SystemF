package org.systemf.compiler.semantic.util;

import org.antlr.v4.runtime.ParserRuleContext;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.parser.SysYParser;
import org.systemf.compiler.semantic.type.*;
import org.systemf.compiler.semantic.value.ValueAndType;
import org.systemf.compiler.semantic.value.ValueClass;

import java.util.HashMap;

public class SysYTypeUtil {
	public static SysYType applyArrayPostfix(SysYType type, SysYParser.ArrayPostfixContext postfix) {
		var dimension = postfix.arrayPostfixSingle().size();
		for (int i = 0; i < dimension; ++i) type = new SysYRoughArray(type);
		return type;
	}

	public static long assertArraySize(long index) {
		if (index < 1) throw new IllegalArgumentException("Invalid array size " + index);
		return index;
	}

	public static SysYType applyArrayPostfix(SysYType type, SysYParser.ArrayPostfixContext postfix,
			HashMap<ParserRuleContext, Value> valueMap) {
		for (var post : postfix.arrayPostfixSingle().reversed())
			type = new SysYArray(type, SysYTypeUtil.assertArraySize(ValueUtil.getConstantInt(valueMap.get(post))));
		return type;
	}

	public static SysYType applyIncompleteArray(SysYType type, SysYParser.IncompleteArrayContext incompleteArray) {
		return new SysYIncompleteArray(type);
	}

	public static SysYType applyVarDefEntry(SysYType type, SysYParser.VarDefEntryContext varDefEntry) {
		return applyArrayPostfix(type, varDefEntry.arrayPostfix());
	}

	public static SysYType applyVarDefEntry(SysYType type, SysYParser.VarDefEntryContext varDefEntry,
			HashMap<ParserRuleContext, Value> valueMap) {
		return applyArrayPostfix(type, varDefEntry.arrayPostfix(), valueMap);
	}

	public static SysYType typeFromBasicType(SysYParser.BasicTypeContext basicType) {
		if (basicType.INT() != null) return SysYInt.INT;
		if (basicType.FLOAT() != null) return SysYFloat.FLOAT;
		throw new IllegalArgumentException("Unknown basic type " + basicType.getText());
	}

	public static SysYType typeFromRetType(SysYParser.RetTypeContext retType) {
		if (retType.basicType() != null) return typeFromBasicType(retType.basicType());
		return SysYVoid.VOID;
	}

	public static SysYType typeFromFuncParam(SysYParser.FuncParamContext funcParam) {
		var result = typeFromBasicType(funcParam.type);
		if (funcParam.arrayPostfix() != null) result = applyArrayPostfix(result, funcParam.arrayPostfix());
		if (funcParam.incompleteArray() != null) result = applyIncompleteArray(result, funcParam.incompleteArray());
		return result;
	}

	public static ValueClass valueClassFromConstPrefix(SysYParser.ConstPrefixContext constPrefix) {
		if (constPrefix.CONST() == null) return ValueClass.LEFT;
		return ValueClass.RIGHT;
	}

	public static SysYNumeric elevatedType(SysYNumeric a, SysYNumeric b) {
		if (a.equals(b)) return b;
		return a;
	}

	public static boolean shouldInline(ValueAndType value) {
		var type = value.type();
		return value.valueClass() == ValueClass.RIGHT && (type == SysYInt.INT || type == SysYFloat.FLOAT);
	}
}