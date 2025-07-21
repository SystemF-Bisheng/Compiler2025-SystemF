package org.systemf.compiler.interpreter;

import org.systemf.compiler.interpreter.value.*;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.type.Array;
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