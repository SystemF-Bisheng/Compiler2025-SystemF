package org.systemf.compiler.lower.rv64gc.instruction;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.instruction.PotentialBlockSensitive;
import org.systemf.compiler.ir.value.instruction.PotentialNonRepeatable;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;

import java.util.Collections;
import java.util.Set;

/**
 * Placeholder pseudo-instruction, occupy a virtual register without specifying the content
 * <p>
 * Should be placed at the header of a basic block
 */
public class RVRegPlaceholder extends DummyValueNonTerminal implements PotentialNonRepeatable, PotentialBlockSensitive {
	public RVRegPlaceholder(Type type, String name) {
		super(type, name);
	}

	@Override
	public String dumpInstructionBody() {
		return "placeholder " + type;
	}

	@Override
	public Set<ITracked> getDependency() {
		return Collections.emptySet();
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void unregister() {
	}
}
