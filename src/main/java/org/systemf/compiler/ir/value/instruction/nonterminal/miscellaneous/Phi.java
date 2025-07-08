package org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Arrays;

public class Phi extends DummyValueNonTerminal {
	private final BasicBlock[] incomingBlocks;
	private final Value[] incomingValues;

	public Phi(Type type, String name, BasicBlock[] incomingBlocks, Value[] incomingValues) {
		super(type, name);
		if (incomingBlocks.length == 0)
			throw new IllegalArgumentException("At least one incoming block must be specified");
		if (incomingBlocks.length != incomingValues.length)
			throw new IllegalArgumentException("Incoming blocks and values must have the same length");
		var valueType = incomingValues[0].getType();
		if (!(valueType instanceof Sized))
			throw new IllegalArgumentException("The type of incoming values must be sized");
		if (!Arrays.stream(incomingValues).map(Value::getType).allMatch(valueType::equals))
			throw new IllegalArgumentException("The types of incoming values must be the same");

		this.incomingBlocks = Arrays.copyOf(incomingBlocks, incomingBlocks.length);
		this.incomingValues = Arrays.copyOf(incomingValues, incomingValues.length);
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