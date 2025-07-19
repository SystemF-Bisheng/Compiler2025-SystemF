package org.systemf.compiler.ir.value.instruction.nonterminal.memory;

import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.InstructionVisitor;
import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.type.interfaces.Atom;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyNonTerminal;
import org.systemf.compiler.ir.value.util.ValueUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Store extends DummyNonTerminal {
	private Value src;
	private Value dest;

	public Store(Value src, Value dest) {
		setSrc(src);
		setDest(dest);
	}

	@Override
	public String toString() {
		return String.format("store %s, %s", ValueUtil.dumpIdentifier(src), ValueUtil.dumpIdentifier(dest));
	}

	@Override
	public Set<ITracked> getDependency() {
		return new HashSet<>(Arrays.asList(src, dest));
	}

	@Override
	public void replaceAll(ITracked oldValue, ITracked newValue) {
		if (src == oldValue) setSrc((Value) newValue);
		if (dest == oldValue) setDest((Value) newValue);
	}

	@Override
	public <T> T accept(InstructionVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public void unregister() {
		if (src != null) src.unregisterDependant(this);
		if (dest != null) dest.unregisterDependant(this);
	}

	public Value getSrc() {
		return src;
	}

	public void setSrc(Value src) {
		if (!(src.getType() instanceof Sized))
			throw new IllegalArgumentException("The type of the source must be sized");
		if (!(src instanceof Constant || src.getType() instanceof Atom))
			throw new IllegalArgumentException("If the type of the source is not atom, the source must be a constant");
		if (this.src != null) this.src.unregisterDependant(this);
		this.src = src;
		src.registerDependant(this);
	}

	public Value getDest() {
		return dest;
	}

	public void setDest(Value dest) {
		if (!(dest.getType() instanceof Pointer))
			throw new IllegalArgumentException("The type of the destination must be a pointer type");
		if (this.dest != null) this.dest.unregisterDependant(this);
		this.dest = dest;
		dest.registerDependant(this);
	}
}