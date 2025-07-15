package org.systemf.compiler.interpreter;

import org.systemf.compiler.interpreter.value.ExecutionValue;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;

import java.util.HashMap;
import java.util.Map;

public class ExecutionContext {
	private BasicBlock currentBlock;
	private final Function currentFunction;
	private int currentInstructionIndex;
	private final Map<Value, ExecutionValue> localVariables;
	private Value callee;

	public ExecutionContext( BasicBlock currentBlock, Function currentFunction, int currentInstructionIndex, Value callee) {
		this.currentBlock = currentBlock;
		this.currentFunction = currentFunction;
		this.currentInstructionIndex = currentInstructionIndex;
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

	public int getCurrentInstructionIndex() {
		return currentInstructionIndex;
	}

	public void setCurrentInstructionIndex(int currentInstructionIndex) {
		this.currentInstructionIndex = currentInstructionIndex;
	}

	public void setCurrentBlock(BasicBlock currentBlock) {
		this.currentBlock = currentBlock;
	}

	public ExecutionValue getValue(Value variable) {
		return localVariables.get(variable);
	}

	public void setValue(Value variable, ExecutionValue executionValue) {
		ExecutionValue existingValue = localVariables.get(variable);
		if (existingValue == null) {
			localVariables.put(variable, executionValue);
			return;
		}
		existingValue.setValue(executionValue);;
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
