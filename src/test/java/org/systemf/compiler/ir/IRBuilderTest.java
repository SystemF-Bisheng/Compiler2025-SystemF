package org.systemf.compiler.ir;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalDeclaration;
import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Parameter;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;

import java.util.Arrays;

public class IRBuilderTest {
	public static void main(String[] args) {
		Module module = new Module();
		try (IRBuilder builder = new IRBuilder(module)) {

			final I32 I32 = builder.buildI32Type();
			final Float Float = builder.buildFloatType();

			//GlobalDeclaration
			GlobalDeclaration globalvar1 = builder.buildGlobalDeclaration("g", I32, builder.buildConstantInt(1));

			GlobalDeclaration globalvar2 = builder.buildGlobalDeclaration("g", I32, builder.buildConstantInt(2));

			Array array = builder.buildArrayType(I32, 10);
			var arrayPtr = builder.buildPointerType(array);

			var arrContent = new Constant[10];
			Arrays.fill(arrContent, builder.buildConstantInt(1));
			GlobalDeclaration globalArrayDeclaration = builder.buildGlobalDeclaration("g", array,
					builder.buildConstantArray(I32, arrContent));


			//Function and BasicBlock

			Parameter param = builder.buildParameter(I32, "param");
			Function function = builder.buildFunction("main", I32, param);

			Parameter param1 = builder.buildParameter(I32, "param1");
			Parameter param2 = builder.buildParameter(arrayPtr, "param2");
			Function function1 = builder.buildFunction("function1", I32, param1, param2);

			BasicBlock entryBlock1 = builder.buildBasicBlock(function, "entry");
			BasicBlock block1 = builder.buildBasicBlock(function, "block1");
			BasicBlock entryBlock2 = builder.buildBasicBlock(function1, "entry1");

			//Instructions
			builder.attachToBlockTail(entryBlock1);

			Value pointer1 = builder.buildAlloca(I32, "p1");
			Value pointer2 = builder.buildAlloca(I32, "p2");

			builder.buildStore(builder.buildConstantInt(5), pointer1);
			builder.buildStore(builder.buildConstantInt(10), pointer2);

			Value load1 = builder.buildLoad(pointer1, "var1");
			Value load2 = builder.buildLoad(pointer2, "var2");
			Value res = builder.buildAnd(load1, load2, "result");
			Value call = builder.buildCall(function1, "call", res, res);

			Value add = builder.buildAdd(call, builder.buildConstantInt(1), "add");

			builder.buildBr(block1);

			builder.attachToBlockTail(entryBlock2);
			var ptr0 = builder.buildGetPtr(param2, builder.buildConstantInt(0), "indexZero");
			var value0 = builder.buildLoad(ptr0, "var0");
			Value div = builder.buildSDiv(param1, value0, "div");
			Value mul = builder.buildMul(div, builder.buildConstantInt(2), "mul");

			builder.buildRet(mul);

			module.dump(System.out);

			IRValidator irValidator = new IRValidator();
			irValidator.check(module);
			System.out.println(irValidator.getErrorMessage());
		}
	}
}