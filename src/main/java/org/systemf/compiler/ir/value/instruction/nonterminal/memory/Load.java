package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.value.Util.ValueUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

public class Load extends DummyNonTerminal {
	public final Value ptr;

	public Load(String name, Value ptr) {
		super(ptr.getType(), name);
		this.ptr = ptr;
	}

	@Override
	public String toString() {
		return String.format("%%%s = load %%%s", name, ValueUtil.getValueName(ptr));
	}
}