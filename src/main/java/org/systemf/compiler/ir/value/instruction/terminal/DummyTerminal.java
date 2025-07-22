package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.value.instruction.DummyInstruction;
import org.systemf.compiler.ir.value.instruction.PotentialSideEffect;

public abstract class DummyTerminal extends DummyInstruction implements Terminal, PotentialSideEffect {
}