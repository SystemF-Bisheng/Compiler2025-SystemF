package org.systemf.compiler.hir.region;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.value.instruction.Instruction;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.stream.Stream;

public class Region {
	public final LinkedHashSet<BasicBlock> blocks = new LinkedHashSet<>();
	private BasicBlock entryBlock;

	public Region() {
	}

	public void insertBlock(BasicBlock block) {
		blocks.add(block);
	}

	public void deleteBlock(BasicBlock block) {
		blocks.remove(block);
	}

	public BasicBlock getEntryBlock() {
		return entryBlock;
	}

	public void setEntryBlock(BasicBlock entryBlock) {
		this.entryBlock = entryBlock;
	}

	public SequencedSet<BasicBlock> getBlocks() {
		return Collections.unmodifiableSequencedSet(blocks);
	}

	public Stream<Instruction> allInstructions() {
		return blocks.stream().flatMap(block -> block.instructions.stream());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		// ! indentation is weird, easy to solve but many coding
		sb.append(" {\n");
		for (var block : blocks) {
			sb.append(block.toString());
		}
		sb.append("}\n");
		return sb.toString();
	}
}
