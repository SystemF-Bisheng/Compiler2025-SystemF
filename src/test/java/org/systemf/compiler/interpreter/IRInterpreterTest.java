package org.systemf.compiler.interpreter;

import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.IRValidator;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Parameter;
import org.systemf.compiler.ir.value.constant.ConstantInt;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Add;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.ICmp;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.Sub;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.Call;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Alloca;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;

import static org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp.LE;

public class IRInterpreterTest {

	public static void main(String[] args) {

		int n = 10; // Change this value to test different Fibonacci numbers

		Module module = new Module();
		try (IRBuilder builder = new IRBuilder(module)) {
			final I32 I32 = builder.buildI32Type();
			final Float Float = builder.buildFloatType();
			// Fibonacci to test recursion

			GlobalVariable g1 = builder.buildGlobalVariable("g1", I32, builder.buildConstantInt(n));

			Function main = builder.buildFunction("main", I32);

			Parameter param1 = builder.buildParameter(I32, "param1");
			Function fib = builder.buildFunction("fib", I32, param1);

			BasicBlock entry = builder.buildBasicBlock(main, "entry");
			BasicBlock fibEntry = builder.buildBasicBlock(fib, "entry");

			builder.attachToBlockTail(entry);
			Load loadG1 = builder.buildLoad(g1, "loadG1");
			var call = builder.buildCall(fib, "fibcall", loadG1);
			builder.buildRet(call);

			builder.attachToBlockTail(fibEntry);
			Alloca alloca2 = builder.buildAlloca(I32, "alloca1");
			Alloca alloca3 = builder.buildAlloca(I32, "alloca");
			builder.buildStore(param1, alloca3);
			Load load4 = builder.buildLoad(alloca3, "load");
			ICmp cmp5 = builder.buildICmp(load4, builder.buildConstantInt(1), "cmp", LE);
			BasicBlock block1 = builder.buildBasicBlock(fib, "1");
			BasicBlock block2 = builder.buildBasicBlock(fib, "2");
			BasicBlock returnBlock = builder.buildBasicBlock(fib, "return");

			CondBr condBr = builder.buildCondBr(cmp5, block1, block2);

			builder.attachToBlockTail(block1);
			Load load7 = builder.buildLoad(alloca3, "load2");
			Store store = builder.buildStore(load7, alloca2);
			builder.buildBr(returnBlock);

			builder.attachToBlockTail(block2);
			Load load9 = builder.buildLoad(alloca3, "load3");
			Sub sub10 = builder.buildSub(load9, builder.buildConstantInt(1), "sub");
			Call call11 = builder.buildCall(fib, "fibcall", sub10);
			Load load12 = builder.buildLoad(alloca3, "load4");
			Sub sub13 = builder.buildSub(load12, builder.buildConstantInt(2), "sub2");
			Call call14 = builder.buildCall(fib, "fibcall", sub13);
			Add add15 = builder.buildAdd(call11, call14, "add");
			builder.buildStore(add15, alloca2);
			builder.buildBr(returnBlock);

			builder.attachToBlockTail(returnBlock);
			Load load16 = builder.buildLoad(alloca2, "load5");
			builder.buildRet(load16);


			IRValidator irValidator = new IRValidator();
			if (irValidator.check(module)){
				IRInterpreter irInterpreter = new IRInterpreter();
				irInterpreter.execute(module);
				System.out.println("fib("+ n +") " + "Expected: " + fib(n) + ", got: " + irInterpreter.getMainRet());
				if (irInterpreter.getMainRet() == fib(10)) {
					System.out.println("passed");
				}else  {
					System.out.println("failed");
				}

			}
		}
	}

	private static int fib(int n) {
		if (n <= 1) {
			return n;
		}
		return fib(n - 1) + fib(n - 2);
	}
}
