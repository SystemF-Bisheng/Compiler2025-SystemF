package org.systemf.compiler.ir.value.instruction.nonterminal.invoke;

import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Arrays;

public abstract class AbstractCall extends DummyNonTerminal {
	public final Value func;
	public final Value[] args;

	protected AbstractCall(Value func, Value... args) {
		this.func = func;
		this.args = Arrays.copyOf(args, args.length);
	}

	protected String dumpCallBody() {
		StringBuilder result = new StringBuilder();
		result.append(((INamed) func).getName());
		result.append("(");
		for (int i = 0; i < args.length; i++) {
			if (i > 0) result.append(", ");
			result.append(ValueUtil.dumpIdentifier(args[i]));
		}
		result.append(")");
		return result.toString();
	}
}