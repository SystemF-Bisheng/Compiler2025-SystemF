package org.systemf.compiler.interpreter;

import org.systemf.compiler.interpreter.value.ExecutionValue;
import org.systemf.compiler.interpreter.value.FloatValue;
import org.systemf.compiler.interpreter.value.IntValue;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.Float;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExecutionContext {
	private BasicBlock currentBlock;
	private final Function currentFunction;
	private Iterator<Instruction> currentInstruction;
	private final Map<Value, ExecutionValue> localVariables;
	private Value callee;

	public ExecutionContext(BasicBlock currentBlock, Function currentFunction, Value callee) {
		this.currentBlock = currentBlock;
		this.currentFunction = currentFunction;
		this.currentInstruction = currentBlock.instructions.iterator();
		this.localVariables = new HashMap<>();
		this.callee = callee;
	}

	public Map<Value, ExecutionValue> getLocalVariables() {
		return localVariables;
	}

	public BasicBlock getCurrentBlock() {
		return currentBlock;
	}

	public Function getCurrentFunction() {
		return currentFunction;
	}

	public Iterator<Instruction> getCurrentInstruction() {
		return currentInstruction;
	}

	public void setCurrentInstruction(Iterator<Instruction> currentInstruction) {
		this.currentInstruction = currentInstruction;
	}

	public void setCurrentBlock(BasicBlock currentBlock) {
		this.currentBlock = currentBlock;
		this.currentInstruction = currentBlock.instructions.iterator();
	}

	public ExecutionValue getValue(Value variable) {
		return localVariables.get(variable);
	}

	public void setValue(Value variable, ExecutionValue executionValue) {
		ExecutionValue existingValue = localVariables.get(variable);
		if (existingValue == null) {
			ExecutionValue newValue = executionValue;
			if (variable.getType() instanceof Array) {
				localVariables.put(variable, executionValue);
				return;
			}
			if (variable.getType() instanceof I32) {
				newValue = new IntValue(0);
			}else if (variable.getType() instanceof Float) {
				newValue = new FloatValue(0);
			}
			newValue.setValue(executionValue);
			localVariables.put(variable, newValue);
		} else existingValue.setValue(executionValue);
	}

	public void insertValue(Value variable, ExecutionValue value) {
		localVariables.put(variable, value);
	}

	public Value getCallee() {
		return callee;
	}

	public void setCallee(Value callee) {
		this.callee = callee;
	}

}