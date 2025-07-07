package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyValueNonTerminal;

public class Load extends DummyValueNonTerminal {
	public final Value ptr;

	public Load(String name, Value ptr) {
		super(TypeUtil.getElementType(ptr.getType()), name);
		this.ptr = ptr;
	}
}