package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

public class Load extends DummyValueNonTerminal {
	public final Value ptr;

	public Load(String name, Value ptr) {
		super(ptr.getType(), name);
		this.ptr = ptr;
	}

	@Override
	public String dumpInstructionBody() {
		return String.format("load %s", ValueUtil.dumpIdentifier(ptr));
	}
}