package org.systemf.compiler.hir.value.instruction.nonterminal.scf;

import org.systemf.compiler.hir.region.Region;
import org.systemf.compiler.hir.value.instruction.IRegionHolder;
import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.PotentialBlockSensitive;
import org.systemf.compiler.ir.value.instruction.PotentialSequential;
import org.systemf.compiler.ir.value.instruction.PotentialSideEffect;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

import java.util.SequencedSet;

public class If extends DummyNonTerminal implements IRegionHolder, PotentialSideEffect, PotentialBlockSensitive,
		PotentialSequential {
	final Region condRegion = new Region(), thenRegion = new Region(), elseRegion;

	public If(boolean hasElse) {
		this.elseRegion = hasElse ? new Region() : null;
	}

	public Region getCondRegion() {
		return condRegion;
	}

	public boolean hasElseRegion() {
		return elseRegion != null;
	}

	public Region getElseRegion() {
		return elseRegion;
	}

	@Override
	public SequencedSet<ITracked> getDependency() {
		// TODO
		throw new UnsupportedOperationException("Unimplemented method `getDependency");
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
		for (BasicBlock block : condRegion.getBlocks()) {
			for (Instruction inst : block.instructions) {
				inst.replaceAll(oldValue, newValue);
			}
		}
		if (hasElseRegion()) {
			for (BasicBlock block : elseRegion.getBlocks()) {
				for (Instruction inst : block.instructions) {
					inst.replaceAll(oldValue, newValue);
				}
			}
		}
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void unregister() {
		// TODO
		throw new UnsupportedOperationException("Unimplemented method 'unregister'");
	}
}
