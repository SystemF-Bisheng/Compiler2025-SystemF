package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

public class Store extends DummyNonTerminal {
	private Value src;
	private Value dest;

	public Store(Value src, Value dest) {
		setSrc(src);
		setDest(dest);
	}

	@Override
	public String toString() {
		return String.format("store %s, %s", ValueUtil.dumpIdentifier(src), ValueUtil.dumpIdentifier(dest));
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	public Value getSrc() {
		return src;
	}

	public void setSrc(Value src) {
		if (!(src.getType() instanceof Sized))
			throw new IllegalArgumentException("The type of the source must be sized");
		this.src = src;
	}

	public Value getDest() {
		return dest;
	}

	public void setDest(Value dest) {
		if (!(dest.getType() instanceof Pointer))
			throw new IllegalArgumentException("The type of the destination must be a pointer type");
		this.dest = dest;
	}
}