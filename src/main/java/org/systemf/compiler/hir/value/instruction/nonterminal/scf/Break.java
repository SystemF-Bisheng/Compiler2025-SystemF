package org.systemf.compiler.hir.value.instruction.nonterminal.scf;

import java.util.Collections;
import java.util.SequencedSet;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.value.instruction.terminal.DummyTerminal;

public class Break extends DummyTerminal {
  public Break() {
  }

  @Override
  public String toString() {
    return "break";
  }

  @Override
  public SequencedSet<ITracked> getDependency() {
    return Collections.emptySortedSet();
  }

  @Override
  public void replaceAll(ITracked oldValue, ITracked newValue) {
    /* ignore */
  }

  @Override
  public <T> T accept(InstructionVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public void unregister() {
    /* ignore */
  }

  
}
