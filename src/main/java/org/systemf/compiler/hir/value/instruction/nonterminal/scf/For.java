package org.systemf.compiler.hir.value.instruction.nonterminal.scf;

import java.util.SequencedSet;

import org.systemf.compiler.hir.region.Region;
import org.systemf.compiler.hir.value.instruction.IRegionHolder;
import org.systemf.compiler.hir.value.loop.IndexValue;
import org.systemf.compiler.hir.value.loop.LoopCarrier;
import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.PotentialBlockSensitive;
import org.systemf.compiler.ir.value.instruction.PotentialSequential;
import org.systemf.compiler.ir.value.instruction.PotentialSideEffect;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

public class For extends DummyNonTerminal implements IRegionHolder, PotentialSideEffect, PotentialBlockSensitive, PotentialSequential {
  final private IndexValue index;
  final private LoopCarrier[] loopCarriers;
  final Region bodyRegion = new Region();

  public For(IndexValue index, LoopCarrier... loopCarriers) {
    this.index = index;
    this.loopCarriers = loopCarriers;
  }

  public IndexValue getIndex() {
    return index;
  }

  public LoopCarrier[] getLoopCarriers() {
    return loopCarriers;
  }

  public Region getBodyRegion() {
    return bodyRegion;
  }

  @Override
  public SequencedSet<ITracked> getDependency() {
    // TODO
    throw new UnsupportedOperationException("Unimplemented method `getDependency");
  }

  @Override
  public void replaceAll(ITracked oldValue, ITracked newValue) {
    if (oldValue == index.getStart()) {
      index.setStart((Value) newValue);
    }
    if (oldValue == index.getEnd()) {
      index.setEnd((Value) newValue);
    }
    if (oldValue == index.getStep()) {
      index.setStep((Value) newValue);
    }
    for (var loopCarrier : loopCarriers) {
      if (loopCarrier.getInitializer() == oldValue) {
        loopCarrier.setInitializer((Value) newValue);
      }
    }
    for (BasicBlock block : bodyRegion.getBlocks()) {
      for (Instruction inst : block.instructions) {
        inst.replaceAll(oldValue, newValue);
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
