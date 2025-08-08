package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.global.IFunction;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;

public class RVTailCall extends AbstractCall implements Terminal {
	public RVTailCall(IFunction func, Value... args) {
		super(func, args);
	}

	@Override
	public String toString() {
		return String.format("tail call %s", dumpCallBody());
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
