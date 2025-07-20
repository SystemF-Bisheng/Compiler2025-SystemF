package org.systemf.compiler.ir.value.instruction.nonterminal.invoke;

import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Void;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.type.util.TypeUtil;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class Call extends AbstractCall implements Value, INamed {
	private final String name;
	private final Type type;
	private final Map<Instruction, Integer> dependant = new WeakHashMap<>();

	public Call(String name, Value func, Value... args) {
		super(func, args);
		this.name = name;
		this.type = TypeUtil.getReturnType(func.getType());
		if (Void.INSTANCE.equals(type))
			throw new IllegalArgumentException("Valued call inst doesn't accept functions returning void");
	}

	@Override
	public void setFunction(Value func) {
		var newRet = TypeUtil.getReturnType(func.getType());
		if (type != null) TypeUtil.assertConvertible(newRet, type, "Illegal return type");
		super.setFunction(func);
	}

	@Override
	public Set<Instruction> getDependant() {
		return Collections.unmodifiableSet(dependant.keySet());
	}

	@Override
	public void registerDependant(Instruction instruction) {
		dependant.compute(instruction, (_, cnt) -> cnt == null ? 1 : cnt + 1);
	}

	@Override
	public void unregisterDependant(Instruction instruction) {
		dependant.compute(instruction, (_, cnt) -> {
			if (cnt == null) return null;
			if (cnt == 1) return null;
			return cnt - 1;
		});
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format("%s = call %s", ValueUtil.dumpIdentifier(this), dumpCallBody());
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public boolean contentEqual(Value other) {
		if (!(other instanceof Call otherCall)) return false;
		if (!ValueUtil.trivialInterchangeable(func, otherCall.func)) return false;
		if (args.length != otherCall.args.length) return false;
		for (int i = 0; i < args.length; ++i)
			if (!ValueUtil.trivialInterchangeable(args[i], otherCall.args[i])) return false;
		return true;
	}
}