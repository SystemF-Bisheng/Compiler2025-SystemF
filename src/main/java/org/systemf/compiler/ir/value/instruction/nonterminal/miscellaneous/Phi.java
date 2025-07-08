package org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

public class Phi extends DummyValueNonTerminal {
	private final BasicBlock[] incomingBlocks;
	private final Value[] incomingValues;

	public Phi(Type type, String name, BasicBlock[] incomingBlocks, Value[] incomingValues) {
		super(type, name);
		this.incomingBlocks = incomingBlocks;
		this.incomingValues = incomingValues;
	}

	@Override
	public String dumpInstructionBody() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < incomingBlocks.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append("[ ");
			sb.append(ValueUtil.dumpIdentifier(incomingValues[i])).append(", ").append(incomingBlocks[i].getName());
			sb.append(" ]");
		}
		return sb.toString();
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}