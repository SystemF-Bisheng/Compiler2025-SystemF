package org.systemf.compiler.ir.block;

import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;

import java.util.LinkedList;

public class BasicBlock implements INamed {
	final private String name;
	public final LinkedList<Instruction> instructions = new LinkedList<>();

	public BasicBlock(String name) {
		this.name = name;
	}

	public void insertInstruction(Instruction inst) {
		instructions.add(inst);
	}

	public Instruction getLastInstruction() {
		if (instructions.isEmpty()) return null;
		return instructions.getLast();
	}

	public Terminal getTerminator() {
		if (!(getLastInstruction() instanceof Terminal term)) return null;
		return term;
	}

	public boolean isTerminated() {
		return getTerminator() != null;
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