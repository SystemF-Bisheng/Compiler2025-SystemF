package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.I32;
import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.type.interfaces.Indexable;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

public class GetPtr extends DummyValueNonTerminal {
	private Value arrayPtr;
	private Value index;

	public GetPtr(String name, Value arrayPtr, Value index) {
		super(new Pointer(TypeUtil.getElementType(TypeUtil.getElementType(arrayPtr.getType()))), name);
		setArrayPtr(arrayPtr);
		setIndex(index);
	}

	@Override
	public String dumpInstructionBody() {
		return String.format("getptr %s, %s", ValueUtil.dumpIdentifier(arrayPtr), ValueUtil.dumpIdentifier(index));
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	public Value getArrayPtr() {
		return arrayPtr;
	}

	public void setArrayPtr(Value arrayPtr) {
		var ptrType = arrayPtr.getType();
		if (!(ptrType instanceof Pointer ptr))
			throw new IllegalArgumentException("The type of the pointer must be a pointer type");
		if (!(ptr.getElementType() instanceof Indexable))
			throw new IllegalArgumentException("The element type of the pointer must be indexable");
		this.arrayPtr = arrayPtr;
	}

	public Value getIndex() {
		return index;
	}

	public void setIndex(Value index) {
		TypeUtil.assertConvertible(index.getType(), I32.INSTANCE, "Illegal index");
		this.index = index;
	}
}