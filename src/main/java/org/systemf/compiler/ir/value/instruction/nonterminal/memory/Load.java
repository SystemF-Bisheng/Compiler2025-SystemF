package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

public class Load extends DummyValueNonTerminal {
	public final Value ptr;

	public Load(String name, Value ptr) {
		super(TypeUtil.getElementType(ptr.getType()), name);
		if (!(ptr.getType() instanceof Pointer ptrType))
			throw new IllegalArgumentException("The type of the operand must be a pointer type");
		if (!(ptrType.getElementType() instanceof Sized))
			throw new IllegalArgumentException("The element type of the pointer must be sized");
		this.ptr = ptr;
	}

	@Override
	public String dumpInstructionBody() {
		return String.format("load %s", ValueUtil.dumpIdentifier(ptr));
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}