package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.type.interfaces.Indexable;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;

public class GetPtr extends DummyValueNonTerminal {
	public final Value arrayPtr, index;

	public GetPtr(String name, Value arrayPtr, Value index) {
		super(new Pointer(TypeUtil.getElementType(TypeUtil.getElementType(arrayPtr.getType()))), name);
		var ptrType = arrayPtr.getType();
		if (!(ptrType instanceof Pointer ptr))
			throw new IllegalArgumentException("The type of the pointer must be a pointer type");
		if (!(ptr.getElementType() instanceof Indexable))
			throw new IllegalArgumentException("The element type of the pointer must be indexable");
		this.arrayPtr = arrayPtr;
		this.index = index;
	}

	@Override
	public String dumpInstructionBody() {
		return String.format("getptr %s, %s", arrayPtr.getType(), index.getType());
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}