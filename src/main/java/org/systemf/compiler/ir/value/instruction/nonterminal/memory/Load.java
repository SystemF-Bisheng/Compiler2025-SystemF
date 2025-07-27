package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.type.interfaces.Atom;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.PotentialBlockSensitive;
import org.systemf.compiler.ir.value.instruction.PotentialNonRepeatable;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Collections;
import java.util.Set;

public class Load extends DummyValueNonTerminal implements PotentialNonRepeatable, PotentialBlockSensitive {
	private Value ptr;

	public Load(String name, Value ptr) {
		super(TypeUtil.getElementType(ptr.getType()), name);
		setPointer(ptr);
	}

	@Override
	public String dumpInstructionBody() {
		return String.format("load %s", ValueUtil.dumpIdentifier(ptr));
	}

	@Override
	public Set<ITracked> getDependency() {
		return Collections.singleton(ptr);
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
		if (ptr == oldValue) setPointer((Value) newValue);
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void unregister() {
		if (ptr != null) ptr.unregisterDependant(this);
	}

	public Value getPointer() {
		return ptr;
	}

	public void setPointer(Value ptr) {
		if (!(ptr.getType() instanceof Pointer ptrType))
			throw new IllegalArgumentException("The type of the operand must be a pointer type");
		var elementType = ptrType.getElementType();
		if (!(elementType instanceof Atom))
			throw new IllegalArgumentException("The element type of the pointer must be atom");
		TypeUtil.assertConvertible(elementType, type, "Illegal pointer");
		if (this.ptr != null) this.ptr.unregisterDependant(this);
		this.ptr = ptr;
		ptr.registerDependant(this);
	}

	@Override
	public boolean contentEqual(Value other) {
		if (!(other instanceof Load load)) return false;
		return ValueUtil.trivialInterchangeable(ptr, load.ptr);
	}
}