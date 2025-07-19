package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;

import java.util.Collections;
import java.util.Set;

public class Alloca extends DummyValueNonTerminal {
	public final Sized valueType;

	public Alloca(String name, Sized type) {
		super(new Pointer(type), name);
		this.valueType = type;
	}

	@Override
	public String dumpInstructionBody() {
		return String.format("alloca %s", valueType.getName());
	}

	@Override
	public Set<Value> getDependency() {
		return Collections.emptySet();
	}

	@Override
	public void replaceAll(Value oldValue, Value newValue) {
	}

	@Override
	public void replaceAll(BasicBlock oldBlock, BasicBlock newBlock) {}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void unregister() {
	}
}