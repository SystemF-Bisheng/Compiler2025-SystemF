package org.systemf.compiler.interpreter;

import org.systemf.compiler.interpreter.value.*;
import org.systemf.compiler.ir.InstructionVisitorBase;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.ExternalFunction;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.constant.ConstantArray;
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
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.ir.value.instruction.terminal.Br;
import org.systemf.compiler.ir.value.instruction.terminal.CondBr;
import org.systemf.compiler.ir.value.instruction.terminal.Ret;
import org.systemf.compiler.ir.value.instruction.terminal.RetVoid;

import java.io.PrintStream;
import java.util.*;

public class IRInterpreter extends InstructionVisitorBase<ExecutionValue> {

	private final List<ExecutionContext> executionContextsStack = new ArrayList<>();
	private final Map<Value, ExecutionValue> globalVarMap = new HashMap<>();
	private ExecutionValue mainReturnValue = null;
	private Scanner input;
	private PrintStream output;

	public void execute(Module module, Scanner input, PrintStream output) {
		mainReturnValue = null;
		executionContextsStack.clear();
		this.input = input;
		this.output = output;
		Function main = module.getFunction("main");
		if (main == null) {
			throw new RuntimeException("main function not found.");
		}
		initializeGlobalVariable(module);
		ExecutionContext executionContext = new ExecutionContext(main.getEntryBlock(), main, null);
		executionContextsStack.add(executionContext);
		ExecutionValue returnValue = null;
		while (!executionContextsStack.isEmpty()) {
			returnValue = executeOnce();
		}
		mainReturnValue = returnValue;
	}

	public int getMainRet() {
		if (mainReturnValue instanceof IntValue intValue) {
			return intValue.getValue() & 0xFF; // Ensure it fits in an 8-bit value
		} else throw new IllegalStateException("Main return value is not an IntValue: " + mainReturnValue);
	}

	private void initializeGlobalVariable(Module module) {
		globalVarMap.clear();
		for (var entry : module.getGlobalDeclarations().entrySet()) {
			GlobalVariable globalVariable = entry.getValue();
			globalVarMap.put(globalVariable,
					formExecutionValue(globalVariable.valueType, globalVariable.getInitializer()));
		}
	}

	private int getNextInputInt() {
		return Integer.decode(getNext());
	}

	private float getNextInputFloat() {
		String nextInput = getNext();
		return java.lang.Float.parseFloat(nextInput);
	}

	private String getNext() {
		return input.next();
	}

	private char getNextInputChar() {
		input.useDelimiter("");
		var res = input.next().charAt(0);
		input.reset();
		return res;
	}

	private ExecutionValue formExecutionValue(Type type, Constant constant) {
		if (type instanceof Array) {
			List<ExecutionValue> values = new ArrayList<>();
			flattenArrayValue((ConstantArray) constant, values);
			int size = values.size();
			ArrayValue arrayValue = new ArrayValue(values.toArray(new ExecutionValue[size]));
			return new PointerValue(arrayValue, type);
		}
		return switch (constant) {
			case ConstantInt intValue -> newInt((int) intValue.value);
			case ConstantFloat constantFloat -> newFloat((float) constantFloat.value);
			default -> newInt(0);
		};
	}

	private void flattenArrayValue(ConstantArray constant, List<ExecutionValue> list) {
		for (int i = 0; i < constant.getSize(); i++) {
			if (constant.getContent(i) instanceof ConstantArray nestedArray) {
				flattenArrayValue(nestedArray, list);
			}else  {
				list.add(formExecutionValue(constant.getElementType(), constant.getContent(i)));
			}
		}
	}

	private ExecutionValue formExecutionValue(Type type) {
		if (type instanceof Array array) {
			int length = array.length;
			while (array.getElementType() instanceof Array nestedArray) {
				length *= nestedArray.length;
				array = (Array) array.getElementType();
			}
			List<ExecutionValue> values = new ArrayList<>(length);
			var elementType = array.getElementType();
			for (int i = 0; i < length; i++) {
				values.add(formExecutionValue(elementType));
			}
			int size = values.size();
			ArrayValue arrayValue =  new ArrayValue(values.toArray(new ExecutionValue[size]));
			return new PointerValue(arrayValue, type);
		}
		return switch (type) {
			case I32 ignored -> newInt(0);
			case Float ignored -> newFloat(0);
			default -> throw new IllegalArgumentException("Type is not an I32 or Float type.");
		};
	}

	private ExecutionValue executeOnce() {
		ExecutionContext currentContext = executionContextsStack.getLast();
		Instruction instruction = currentContext.getCurrentInstruction().next();
		return instruction.accept(this);
	}

	private BasicBlock lastBlock;

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

		if (x instanceof IntValue leftValue && y instanceof IntValue rightValue) {
			int left = leftValue.getValue();
			int right = rightValue.getValue();
			switch (dummyBinary) {
				case Add ignored -> {return newInt(left + right);}
				case Sub ignored -> {return newInt(left - right);}
				case Mul ignored -> {return newInt(left * right);}
				case SDiv ignored -> {
					if (right == 0) {
						throw new ArithmeticException("Division by zero in SDiv operation.");
					}
					return newInt(left / right);
				}
				case SRem ignored -> {
					if (right == 0) {
						throw new ArithmeticException("Division by zero in SRem operation.");
					}
					return newInt(left % right);
				}
				case And ignored -> {return newInt(left & right);}
				case Or ignored -> {return newInt(left | right);}
				case Xor ignored -> {return newInt(left ^ right);}
				case Shl ignored -> {return newInt(left << right);}
				case AShr ignored -> {return newInt(left >> right);}
				case LShr ignored -> {return newInt(left >>> right);}
				case ICmp iCmp -> {return executeCmp(iCmp.method, left, right);}
				default -> throw new IllegalStateException("Unexpected left: " + dummyBinary);
			}
		} else {
			float left = toFloat(x);
			float right = toFloat(y);
			switch (dummyBinary) {
				case FAdd ignored -> {return newFloat(left + right);}
				case FSub ignored -> {return newFloat(left - right);}
				case FMul ignored -> {return newFloat(left * right);}
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

	private ExecutionValue executeCmp(CompareOp compareOp, int x, int y) {
		int comparisonResult = Integer.compare(x, y);
		return getCompareValue(compareOp, comparisonResult);
	}

	private ExecutionValue executeCmp(CompareOp compareOp, float x, float y) {
		int comparisonResult = java.lang.Float.compare(x, y);
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
		return newInt(result);
	}

	@Override
	public ExecutionValue visit(FNeg fNeg) {
		ExecutionContext context = executionContextsStack.getLast();
		var x = findValue(fNeg.getX(), context);
		context.insertValue(fNeg, newFloat(-toFloat(x)));
		return null;
	}

	@Override
	public ExecutionValue visit(FpToSi fpToSi) {
		ExecutionContext context = executionContextsStack.getLast();
		var x = findValue(fpToSi.getX(), context);
		context.insertValue(fpToSi, newInt(toInt(x)));
		return null;
	}

	@Override
	public ExecutionValue visit(SiToFp siToFp) {
		ExecutionContext context = executionContextsStack.getLast();
		var x = findValue(siToFp.getX(), context);
		context.insertValue(siToFp, newFloat(toFloat(x)));
		return null;
	}

	@Override
	public ExecutionValue visit(Alloca alloca) {
		ExecutionContext context = executionContextsStack.getLast();
		context.insertValue(alloca, formExecutionValue(alloca.valueType));
		return null;
	}

	@Override
	public ExecutionValue visit(Store store) {
		ExecutionContext context = executionContextsStack.getLast();
		var dest = store.getDest();
		var src = findValue(store.getSrc(), context);
		setValue(dest, src, context);
		return null;
	}

	private ExecutionValue findValue(Value value, Map<Value, ExecutionValue> varMap) {
		if (value instanceof GlobalVariable) {
			return globalVarMap.get(value);
		}
		if (value instanceof ConstantInt constantInt) return newInt((int) constantInt.value);
		if (value instanceof ConstantFloat constantFloat) return newFloat((float) constantFloat.value);
		if (value instanceof ConstantArray constantArray) return formExecutionValue(value.getType(), constantArray);
		return varMap.get(value);
	}

	@Override
	public ExecutionValue visit(Ret ret) {
		ExecutionContext currentContext = executionContextsStack.getLast();
		ExecutionValue returnValue = findValue(ret.getReturnValue(), currentContext);
		Value callee = currentContext.getCallee();
		executionContextsStack.remove(currentContext);
		currentContext = executionContextsStack.isEmpty() ? null : executionContextsStack.getLast();
		if (callee != null) {
			currentContext.insertValue(callee, formExecutionValue(callee.getType()));
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
		if (abstractCall.getFunction() instanceof ExternalFunction) {
			executeExternalFunction(abstractCall);
			return null;
		}
		Function function = (Function) abstractCall.getFunction();
		Value[] arguments = abstractCall.getArgs();
		Value[] formalArgs = function.getFormalArgs();
		ExecutionContext newContext = new ExecutionContext(function.getEntryBlock(), function, null);
		for (int i = 0; i < arguments.length; i++) {
			Value arg = arguments[i];
			ExecutionValue argValue = findValue(arg, context);
			newContext.insertValue(formalArgs[i], argValue);
		}
		if (abstractCall instanceof Call call) newContext.setCallee(call);
		executionContextsStack.add(newContext);
		return null;
	}

	private String formatFloat(float f) {
		var res = java.lang.Float.toHexString(f);
		res = res.replace(".0p", "p");
		var pPos = res.indexOf('p');
		if (res.charAt(pPos + 1) != '-') res = res.replace("p", "p+");
		return res;
	}

	private void executeExternalFunction(AbstractCall abstractCall) {
		ExternalFunction externalFunction = (ExternalFunction) abstractCall.getFunction();
		ExecutionContext context = executionContextsStack.getLast();
		switch (externalFunction.getName()) {
			case "getint" -> {
				int inputValue = getNextInputInt();
				context.insertValue((Call) abstractCall, newInt(inputValue));
			}
			case "putint" -> {
				ExecutionValue value = findValue(abstractCall.getArgs()[0], context);
				if (value instanceof IntValue intValue) {
					output.print(intValue.getValue());
				} else {
					throw new IllegalArgumentException("Expected IntValue for putint, got: " + value);
				}
			}
			case "getfloat" -> {
				float inputValue = getNextInputFloat();
				context.insertValue((Call) abstractCall, newFloat(inputValue));
			}
			case "putfloat" -> {
				ExecutionValue value = findValue(abstractCall.getArgs()[0], context);
				if (value instanceof FloatValue floatValue) {
					output.print(formatFloat(floatValue.getValue()));
				} else {
					throw new IllegalArgumentException("Expected FloatValue for putfloat, got: " + value);
				}
			}
			case "getch" -> {
				int inputValue = getNextInputChar();
				context.insertValue((Call) abstractCall, newInt(inputValue));
			}
			case "putch" -> {
				ExecutionValue value = findValue(abstractCall.getArgs()[0], context);
				if (value instanceof IntValue intValue) {
					output.print((char) intValue.getValue());
				} else {
					throw new IllegalArgumentException("Expected IntValue for putch, got: " + value);
				}
			}
			case "getarray", "getfarray" -> {
				PointerValue arrayValue = (PointerValue) findValue(abstractCall.getArgs()[0], context);
				int length = getNextInputInt();
				for (int i = 0; i < length; i++) {
					if (externalFunction.getName().equals("getarray")) {
						int inputValue = getNextInputInt();
						arrayValue.setValue(i, newInt(inputValue));
					} else {
						float inputValue = getNextInputFloat();
						arrayValue.setValue(i, newFloat(inputValue));
					}
				}
				context.insertValue((Call) abstractCall, newInt(length));
			}
			case "putarray", "putfarray" -> {
				IntValue lengthValue = (IntValue) findValue(abstractCall.getArgs()[0], context);
				PointerValue arrayValue = (PointerValue) findValue(abstractCall.getArgs()[1], context);
				int length = lengthValue.getValue();
				output.print(lengthValue);
				output.print(": ");
				for (int i = 0; i < length; i++) {
					if (i > 0) output.print(" ");
					if (((PointerValue) arrayValue.getValue(i)).getStart() instanceof IntValue intValue) {
						output.print(intValue.getValue());
					} else if (((PointerValue) arrayValue.getValue(i)).getStart() instanceof FloatValue floatValue) {
						output.print(formatFloat(floatValue.getValue()));
					} else {
						throw new IllegalArgumentException("Unexpected value type in array: " + arrayValue.getValue(i));
					}
				}
				output.println();
			}
		}
	}

	@Override
	public ExecutionValue visit(GetPtr getPtr) {
		ExecutionContext context = executionContextsStack.getLast();
		Value basePointer = getPtr.getArrayPtr();
		int index = toInt(findValue(getPtr.getIndex(), context));
		PointerValue baseValue = (PointerValue) findValue(basePointer, context);
		if (baseValue == null) {
			throw new IllegalStateException("Base pointer not found for GetPtr instruction: " + basePointer);
		}
		context.insertValue(getPtr, baseValue.getValue(index));
		return null;
	}

	private ExecutionValue findValue(Value value, ExecutionContext context) {
		return findValue(value, context.getLocalVariables());
	}

	@Override
	public ExecutionValue visit(Load load) {
		ExecutionContext context = executionContextsStack.getLast();
		var src = load.getPointer();
		var value = findValue(src, context);
		if (value == null) {
			throw new IllegalStateException("Value not found for Load instruction: " + src);
		}
		if (value instanceof PointerValue pointerValue && !(pointerValue.type instanceof Array)) {
			value = pointerValue.getStart();
		}
		context.insertValue(load, value.clone());
		return null;
	}

	@Override
	public ExecutionValue visit(Br br) {
		ExecutionContext currentContext = executionContextsStack.getLast();
		BasicBlock targetBlock = br.getTarget();
		lastBlock = currentContext.getCurrentBlock();
//		oldVariables = new HashMap<>(currentContext.getLocalVariables());
		currentContext.setCurrentBlock(targetBlock);
		return null;
	}

	@Override
	public ExecutionValue visit(CondBr condBr) {
		ExecutionContext currentContext = executionContextsStack.getLast();
		var conditionValue = findValue(condBr.getCondition(), currentContext);
		int condition = toInt(conditionValue);
		BasicBlock targetBlock = condition != 0 ? condBr.getTrueTarget() : condBr.getFalseTarget();
		lastBlock = currentContext.getCurrentBlock();
//		oldVariables = new HashMap<>(currentContext.getLocalVariables());
		currentContext.setCurrentBlock(targetBlock);
		return null;
	}

	@Override
	public ExecutionValue visit(Phi inst) {
		ExecutionContext currentContext = executionContextsStack.getLast();
//		var res = findValue(inst.getIncoming().get(lastBlock), oldVariables);
		var res = findValue(inst.getIncoming().get(lastBlock), currentContext.getLocalVariables());
		if (res == null) return null;
		currentContext.insertValue(inst, res);
		return null;
	}

	private void setValue(Value value, ExecutionValue executionValue, ExecutionContext context) {
		if (value instanceof GlobalVariable) {
			setValue(value, executionValue, globalVarMap);
		} else {
			setValue(value, executionValue, context.getLocalVariables());
		}
	}

	public void setValue(Value variable, ExecutionValue executionValue, Map<Value, ExecutionValue> varMap) {
		if (varMap.get(variable) instanceof PointerValue array) {
			array.setValue(executionValue);
			return;
		}
		ExecutionValue value = varMap.get(variable);
		value.setValue(executionValue);
	}

	private float toFloat(ExecutionValue value) {
		if (value instanceof FloatValue f) return f.getValue();
		if (value instanceof IntValue i) return (float) i.getValue();
		throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
	}

	private int toInt(ExecutionValue value) {
		if (value instanceof IntValue i) return i.getValue();
		if (value instanceof FloatValue f) return (int) f.getValue();
		throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
	}

	public static ExecutionValue newInt(int value) {
		return new IntValue(value);
	}

	public static ExecutionValue newFloat(float value) {
		return new FloatValue(value);
	}

	public void dumpExecutionContext() {
		if (executionContextsStack.isEmpty()) {
			System.out.println("No execution context available.");
			return;
		}
		ExecutionContext currentContext = executionContextsStack.getLast();
		System.out.println("Current Block: " + currentContext.getCurrentBlock().getName());
		System.out.println("Current Function: " + currentContext.getCurrentFunction().getName());
		System.out.println("Current Instruction Index: " + currentContext.getCurrentInstruction());
		System.out.println("Local Variables: ");
		for (Map.Entry<Value, ExecutionValue> entry : currentContext.getLocalVariables().entrySet()) {
			System.out.print("  " + entry.getKey() + " = ");
			ExecutionValue value = entry.getValue();
			if (value == null) {
				System.out.println("null");
				continue;
			}
			switch (value) {
				case IntValue intValue -> System.out.println("IntValue: " + intValue.getValue());
				case FloatValue floatValue -> System.out.println("FloatValue: " + floatValue.getValue());
				case ArrayValue arrayValue -> System.out.println("ArrayValue: " + Arrays.toString(arrayValue.values()));
				case PointerValue pointerValue -> {
					if (pointerValue.value instanceof ArrayValue) {
						System.out.println("PointerValue: " + Arrays.toString(pointerValue.getValues()) +
						                   pointerValue.getStartIndex() + " to " + pointerValue.getEndIndex());
					}else if (pointerValue.value instanceof IntValue intValue) {
						System.out.println("PointerValue: " + intValue.getValue());
					}else if (pointerValue.value instanceof FloatValue floatValue) {
						System.out.println("PointerValue: " + floatValue.getValue());
					}
				}
				default -> System.out.println("Unknown Value Type: " + value.getClass().getName());
			}
		}
	}
}