package org.systemf.compiler.ir.block;

import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.WeakHashMap;

public class BasicBlock implements INamed, ITracked {
	public final LinkedList<Instruction> instructions = new LinkedList<>();
	final private String name;
	private final Set<Instruction> dependant = Collections.newSetFromMap(new WeakHashMap<>());

	public BasicBlock(String name) {
		this.name = name;
	}

	public void insertInstruction(Instruction inst) {
		instructions.add(inst);
	}

	public Instruction getFirstInstruction() {
		if (instructions.isEmpty()) return null;
		return instructions.getFirst();
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

	public void destroy() {
		instructions.forEach(Instruction::unregister);
		instructions.clear();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Set<Instruction> getDependant() {
		return Collections.unmodifiableSet(dependant);
	}

	@Override
	public void registerDependant(Instruction instruction) {
		dependant.add(instruction);
	}

	@Override
	public void unregisterDependant(Instruction instruction) {
		dependant.remove(instruction);
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