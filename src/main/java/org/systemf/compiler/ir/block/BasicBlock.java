package org.systemf.compiler.ir.block;

import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.value.instruction.Instruction;

import java.util.ArrayList;

public class BasicBlock implements INamed {
	final private String name;
	final private ArrayList<Instruction> instructions;

	public BasicBlock(String name) {
		this.name = name;
		this.instructions = new ArrayList<>();
	}

	public void insertInstruction(Instruction inst) {
		instructions.add(inst);
	}

	public void insertInstruction(Instruction inst, int index) {
		instructions.add(index, inst);
	}

	public void deleteInstruction(Instruction inst) {
		instructions.remove(inst);
	}

	public void deleteInstruction(int index) {
		instructions.remove(index);
	}

	public int getInstructionCount() {
		return instructions.size();
	}

	public Instruction getInstruction(int index) {
		return instructions.get(index);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(":\n");
		for (Instruction inst : instructions) {
			sb.append("\t");
			sb.append(inst.toString()).append("\n");
		}
		return sb.toString();
	}
}