package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

public class Store extends DummyNonTerminal {
	public final Value src, dest;

	public Store(Value src, Value dest) {
		var srcType = src.getType();
		var destType = dest.getType();
		if (!(destType instanceof Pointer ptr))
			throw new IllegalArgumentException("The type of the destination must be a pointer type");
		if (!(srcType.equals(ptr.getElementType()))) throw new IllegalArgumentException(
				"The type of the source must equal to the element type of the destination");
		if (!(srcType instanceof Sized)) throw new IllegalArgumentException("The type of the source must be sized");
		this.src = src;
		this.dest = dest;
	}

	@Override
	public String toString() {
		return String.format("store %s, %s", ValueUtil.dumpIdentifier(src), ValueUtil.dumpIdentifier(dest));
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}