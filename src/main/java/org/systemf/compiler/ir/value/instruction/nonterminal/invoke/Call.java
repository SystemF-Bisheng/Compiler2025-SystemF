package org.systemf.compiler.ir.value.instruction.nonterminal.invoke;

import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.Util.ValueUtil;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;

public class Call extends DummyNonTerminal {
	public final Value func;
	public final Value[] args;

	public Call(String name, Value func, Value... args) {
		super(TypeUtil.getReturnType(func.getType()), name);
		this.func = func;
		this.args = args;
	}

	@Override
	public String toString() {
		StringBuilder argsString = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
				argsString.append(", ");
			}
			argsString.append("%").append(ValueUtil.getValueName(args[i]));
		}
		return String.format("%%%s = call %s(%s)", name, ((Function)func).getName(), argsString);
	}
}