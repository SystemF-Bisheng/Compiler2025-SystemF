package org.systemf.compiler.semantic.external;

import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.semantic.type.*;
import org.systemf.compiler.semantic.value.ValueAndType;
import org.systemf.compiler.util.Context;

public class SysYExternalRegistry {
	public static void registerSysY(Context<ValueAndType> context) {
		var INT = SysYInt.INT;
		var FLOAT = SysYFloat.FLOAT;
		var VOID = SysYVoid.VOID;

		var IARR = new SysYIncompleteArray(INT);
		var FARR = new SysYIncompleteArray(FLOAT);

		context.define("getint", ValueAndType.ofRight(new SysYFunction(INT)));
		context.define("getch", ValueAndType.ofRight(new SysYFunction(INT)));
		context.define("getfloat", ValueAndType.ofRight(new SysYFunction(FLOAT)));
		context.define("getarray", ValueAndType.ofRight(new SysYFunction(INT, IARR)));
		context.define("getfarray", ValueAndType.ofRight(new SysYFunction(INT, FARR)));
		context.define("putint", ValueAndType.ofRight(new SysYFunction(VOID, INT)));
		context.define("putch", ValueAndType.ofRight(new SysYFunction(VOID, INT)));
		context.define("putfloat", ValueAndType.ofRight(new SysYFunction(VOID, FLOAT)));
		context.define("putarray", ValueAndType.ofRight(new SysYFunction(VOID, INT, IARR)));
		context.define("putfarray", ValueAndType.ofRight(new SysYFunction(VOID, INT, FARR)));
	}

	public static void registerIR(IRBuilder builder) {
		var I32 = builder.buildI32Type();
		var FLOAT = builder.buildFloatType();
		var VOID = builder.buildVoidType();

		var I32ARR = builder.buildPointerType(builder.buildUnsizedArrayType(I32));
		var FARR = builder.buildPointerType(builder.buildUnsizedArrayType(FLOAT));

		builder.buildExternalFunction("getint", I32);
		builder.buildExternalFunction("getch", I32);
		builder.buildExternalFunction("getfloat", FLOAT);
		builder.buildExternalFunction("getarray", I32, I32ARR);
		builder.buildExternalFunction("getfarray", I32, FARR);
		builder.buildExternalFunction("putint", VOID, I32);
		builder.buildExternalFunction("putch", VOID, I32);
		builder.buildExternalFunction("putfloat", VOID, FLOAT);
		builder.buildExternalFunction("putarray", VOID, I32, I32ARR);
		builder.buildExternalFunction("putfarray", VOID, I32, FARR);
	}
}