package org.systemf.compiler.ir.block;

import java.util.ArrayList;

import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.value.instruction.Instruction;

public class BasicBlock implements INamed {
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

	final private String name;

	final private ArrayList<Instruction> instructions;
}