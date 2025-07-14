package org.systemf.compiler.interpreter;

import org.systemf.compiler.interpreter.value.ArrayValue;
import org.systemf.compiler.interpreter.value.ExecutionValue;
import org.systemf.compiler.interpreter.value.FloatValue;
import org.systemf.compiler.interpreter.value.IntValue;
import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.constant.ConstantFloat;
import org.systemf.compiler.ir.value.constant.ConstantInt;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.CompareOp;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.bitwise.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.FpToSi;
import org.systemf.compiler.ir.value.instruction.nonterminal.conversion.SiToFp;
import org.systemf.compiler.ir.value.instruction.nonterminal.farithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.iarithmetic.*;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.Call;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Alloca;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.GetPtr;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.ir.value.instruction.terminal.Ret;
import org.systemf.compiler.ir.value.instruction.terminal.RetVoid;
import org.systemf.compiler.query.EntityProvider;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.global.Function;

import java.util.*;

public class IRInterpreter extends InstructionVisitorBase<ExecutionValue> implements EntityProvider<IRInterpretedResult> {

	private final List<ExecutionContext> executionContextsStack = new ArrayList<>();
	private final Map<Value, ExecutionValue> globalVarMap = new HashMap<>();
	private ExecutionValue mainReturnValue = null;

	@Override
	public IRInterpretedResult produce() {
		throw new UnsupportedOperationException("IRInterpreter is not implemented yet.");
	}

	public void execute(Module module) {
		mainReturnValue = null;
		initializeGlobalVariable(module);
		Function main = module.getFunction("main");
		ExecutionContext executionContext = new ExecutionContext(main.getEntryBlock(), main, 0, null);
		executionContextsStack.add(executionContext);
		ExecutionValue returnValue = null;
		while (!executionContextsStack.isEmpty()) {
			returnValue = executeOnce();
		}
		mainReturnValue = returnValue;
	}

	public Constant getMainRet() {
		if (mainReturnValue == null) {
			throw new IllegalStateException("Main function has not been executed yet.");
		}
		if (mainReturnValue instanceof IntValue intValue) {
			return new ConstantInt(intValue.value());
		} else if (mainReturnValue instanceof FloatValue floatValue) {
			return new ConstantFloat(floatValue.value());
		} else{
		    throw  new IllegalStateException("Not implemented yet: Array return value.");
		}
	}

	private void initializeGlobalVariable(Module module) {
		globalVarMap.clear();
		for (var entry : module.getGlobalDeclarations().entrySet()) {
			GlobalVariable globalVariable = entry.getValue();
			globalVarMap.put(globalVariable, formExecutionValue(globalVariable.getInitializer()));
		}
	}

	private ExecutionValue formExecutionValue(Constant constant) {
		return switch (constant) {
			case ConstantInt intValue -> new IntValue((int) intValue.value);
			case ConstantFloat constantFloat -> new FloatValue((float) constantFloat.value);
			// TODO
//			case ConstantArray constantArray -> new ArrayValue(constantArray.getContent());
			default ->
					throw new IllegalArgumentException("Unsupported constant type: " + constant.getClass().getName());
		};
	}

	private ExecutionValue executeOnce() {
		ExecutionContext currentContext = executionContextsStack.getLast();
		BasicBlock currentBlock = currentContext.getCurrentBlock();
		Instruction instruction = currentBlock.getInstruction(currentContext.getCurrentInstructionIndex());
		currentContext.setCurrentInstructionIndex(currentContext.getCurrentInstructionIndex() + 1);
		return instruction.accept(this);
	}

	private ExecutionValue findValue(Value value, ExecutionContext context) {
		if (value instanceof GlobalVariable) {
			return globalVarMap.get(value);
		}
		if (value instanceof ConstantInt constantInt) return new IntValue((int) constantInt.value);
		if (value instanceof ConstantFloat constantFloat) return new FloatValue((float) constantFloat.value);
		return context.getValue(value);
	}

	@Override
	public ExecutionValue visit(DummyBinary dummyBinary) {
		ExecutionContext context = executionContextsStack.getLast();
		var x = findValue(dummyBinary.getX(), context);
		var y = findValue(dummyBinary.getY(), context);
		if (x == null || y == null) {
			throw new IllegalStateException("Value not found for DummyBinary instruction: " + dummyBinary);
		}
		ExecutionValue result = executeBinaryOperation(x, y, dummyBinary);
		context.insertValue(dummyBinary, result);
		return null;
	}

	private ExecutionValue executeBinaryOperation(ExecutionValue x, ExecutionValue y, DummyBinary dummyBinary) {

		if (x instanceof IntValue(int left) && y instanceof IntValue(int right)) {
			switch (dummyBinary){
				case Add ignored -> {return new IntValue(left + right);}
				case Sub ignored -> {return new IntValue(left - right);}
				case Mul ignored -> {return new IntValue(left * right);}
				case SDiv ignored -> {
					if (right == 0) {
						throw new ArithmeticException("Division by zero in SDiv operation.");
					}
					return new IntValue(left / right);
				}
				case SRem ignored -> {
					if (right == 0) {
						throw new ArithmeticException("Division by zero in SRem operation.");
					}
					return new IntValue(left % right);
				}
				case And ignored -> {return new IntValue(left & right);}
				case Or ignored -> {return new IntValue(left | right);}
				case Xor ignored -> {return new IntValue(left ^ right);}
				case Shl ignored -> {return new IntValue(left << right);}
				case AShr ignored -> {return new IntValue(left >> right);}
				case LShr ignored -> {return new IntValue(left >>> right);}
				case ICmp iCmp -> {return executeCmp(iCmp.method, left, right);}
				default -> throw new IllegalStateException("Unexpected left: " + dummyBinary);
			}
		} else {
			float left = toFloat(x);
			float right = toFloat(y);
			switch (dummyBinary) {
				case FAdd ignored -> {return new FloatValue(left + right);}
				case FSub ignored -> {return new FloatValue(left - right);}
				case FMul ignored -> {return new FloatValue(left * right);}
				case FDiv ignored -> {
					if (right == 0.0f) {
						throw new ArithmeticException("Division by zero in FDiv operation.");
					}
					return new FloatValue(left / right);
				}
				case FCmp fCmp -> {return executeCmp(fCmp.method, left, right);}
				default -> throw new IllegalStateException("Unexpected left: " + dummyBinary);
			}
		}
	}

	public ExecutionValue executeCmp(CompareOp compareOp, int x, int y) {
		int comparisonResult = Integer.compare(x, y);
		return getCompareValue(compareOp, comparisonResult);
	}

	public ExecutionValue executeCmp(CompareOp compareOp, float x, float y) {
		int comparisonResult = Float.compare(x, y);
		return getCompareValue(compareOp, comparisonResult);
	}

	private ExecutionValue getCompareValue(CompareOp compareOp, int comparisonResult) {
		int result = switch (compareOp) {
			case EQ -> comparisonResult == 0 ? 1 : 0;
			case NE -> comparisonResult != 0 ? 1 : 0;
			case LT -> comparisonResult < 0 ? 1 : 0;
			case LE -> comparisonResult <= 0 ? 1 : 0;
			case GT -> comparisonResult > 0 ? 1 : 0;
			case GE -> comparisonResult >= 0 ? 1 : 0;
		};
		return new IntValue(result);
	}

	@Override
	public ExecutionValue visit(FNeg fNeg) {
		ExecutionContext context = executionContextsStack.getLast();
		var x = findValue(fNeg.getX(), context);
		float negatedValue = -toFloat(x);
		ExecutionValue result = new FloatValue(negatedValue);
		context.insertValue(fNeg, result);
		return null;
	}

	@Override
	public  ExecutionValue visit(FpToSi fpToSi) {
		ExecutionContext context = executionContextsStack.getLast();
		var x = findValue(fpToSi.getX(), context);
		int convertedValue = toInt(x);
		ExecutionValue result = new IntValue(convertedValue);
		context.insertValue(fpToSi, result);
		return null;
	}

	@Override
	public ExecutionValue visit(SiToFp siToFp) {
		ExecutionContext context = executionContextsStack.getLast();
		var x = findValue(siToFp.getX(), context);
		float convertedValue = toFloat(x);
		ExecutionValue result = new FloatValue(convertedValue);
		context.insertValue(siToFp, result);
		return null;
	}

	@Override
	public ExecutionValue visit(Alloca alloca) {
		ExecutionContext context = executionContextsStack.getLast();
		context.insertValue(alloca, null);
		return null;
	}

	@Override
	public ExecutionValue visit(Store store) {
		ExecutionContext context = executionContextsStack.getLast();
		var dest = store.getDest();
		var src = findValue(store.getSrc(), context);
		setValue(dest, src, context);
		return  null;
	}

	@Override
	public ExecutionValue visit(Load load) {
		ExecutionContext context = executionContextsStack.getLast();
		var src = load.getPointer();
		var value = findValue(src, context);
		if (value == null) {
			throw new IllegalStateException("Value not found for Load instruction: " + src);
		}
		context.insertValue(load, value);
		return null;
	}

	@Override
	public ExecutionValue visit(Ret ret) {
		ExecutionContext currentContext = executionContextsStack.getLast();
		ExecutionValue returnValue = findValue(ret.getReturnValue(), currentContext);
		Value callee = currentContext.getCallee();
		executionContextsStack.remove(currentContext);
		currentContext = executionContextsStack.isEmpty() ? null : executionContextsStack.getLast();
		if (callee != null) {
			setValue(callee, returnValue, currentContext);
		}
		return returnValue;
	}

	@Override
	public ExecutionValue visit(RetVoid retVoid) {
		ExecutionContext currentContext = executionContextsStack.getLast();
		executionContextsStack.remove(currentContext);
		return null;
	}

	@Override
	public ExecutionValue visit(AbstractCall abstractCall) {
		ExecutionContext context = executionContextsStack.getLast();
		Function function = (Function) abstractCall.getFunction();
		Value[] arguments = abstractCall.getArgs();
		Value[] formalArgs = function.getFormalArgs();
		ExecutionContext newContext = new ExecutionContext(function.getEntryBlock(), function, 0, null);
		for (int i = 0; i < arguments.length; i++) {
			Value arg = arguments[i];
			ExecutionValue argValue = findValue(arg, context);
			newContext.insertValue(formalArgs[i], argValue);
		}
		if (abstractCall instanceof Call call) newContext.setCallee(call);
		executionContextsStack.add(newContext);
		return null;
	}

	@Override
	public ExecutionValue visit(GetPtr getPtr) {
		// TODO
		ExecutionContext context = executionContextsStack.getLast();
		Value basePointer = getPtr.getArrayPtr();
		ExecutionValue baseValue = findValue(basePointer, context);
		if (baseValue == null) {
			throw new IllegalStateException("Base pointer not found for GetPtr instruction: " + basePointer);
		}
		context.insertValue(getPtr, baseValue);
		return null;
	}

	@Override
	public ExecutionValue visit(Br br) {
		ExecutionContext currentContext = executionContextsStack.getLast();
		BasicBlock targetBlock = br.getTarget();
		currentContext.setCurrentBlock(targetBlock);
		currentContext.setCurrentInstructionIndex(0);
		return null;
	}

	@Override
	public ExecutionValue visit(CondBr condBr) {
		ExecutionContext currentContext = executionContextsStack.getLast();
		var conditionValue = findValue(condBr.getCondition(), currentContext);
		int condition = toInt(conditionValue);
		BasicBlock targetBlock = condition != 0 ? condBr.getTrueTarget() : condBr.getFalseTarget();
		currentContext.setCurrentBlock(targetBlock);
		currentContext.setCurrentInstructionIndex(0);
		return null;
	}

	private void setValue(Value value, ExecutionValue executionValue, ExecutionContext context) {
		// TODO: modify executionValue but not replace it
		if (value instanceof GlobalVariable globalVar) {
			ExecutionValue x = globalVarMap.get(value);
		} else {
			context.setValue(value, executionValue);
		}
	}

	private float toFloat(ExecutionValue value) {
	    if (value instanceof FloatValue(float f)) return f;
	    if (value instanceof IntValue(int i)) return (float) i;
        throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
	}

	private int toInt(ExecutionValue value) {
		if (value instanceof IntValue(int i)) return i;
		if (value instanceof FloatValue(float f)) return (int) f;
		throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
	}

	public void dumpExecutionContext() {
		if (executionContextsStack.isEmpty()) {
			System.out.println("No execution context available.");
			return;
		}
		ExecutionContext currentContext = executionContextsStack.getLast();
		System.out.println("Current Block: " + currentContext.getCurrentBlock().getName());
		System.out.println("Current Function: " + currentContext.getCurrentFunction().getName());
		System.out.println("Current Instruction Index: " + currentContext.getCurrentInstructionIndex());
		System.out.println("Local Variables: ");
		for (Map.Entry<Value, ExecutionValue> entry : currentContext.getLocalVariables().entrySet()) {
			System.out.print("  " + entry.getKey() + " = " );
			ExecutionValue value = entry.getValue();
			if (value == null) {
				System.out.println("null");
				continue;
			}
			switch (value) {
				case IntValue intValue -> System.out.println("IntValue: " + intValue.value());
				case FloatValue floatValue -> System.out.println("FloatValue: " + floatValue.value());
				case ArrayValue arrayValue -> System.out.println("ArrayValue: " +
				                                                 Arrays.toString(arrayValue.getValues()));
				default -> System.out.println("Unknown Value Type: " + value.getClass().getName());
			}
		}
	}
}
