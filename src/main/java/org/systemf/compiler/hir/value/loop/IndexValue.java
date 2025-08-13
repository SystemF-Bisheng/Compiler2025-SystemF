package org.systemf.compiler.hir.value.loop;

import org.systemf.compiler.ir.INamed;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.Value;

public class IndexValue extends DummyLoopValue implements INamed {
	final private String name;
	private Value start, end, step;

	public IndexValue(Type type, String name, Value start, Value end, Value step) {
		super(type);
		this.name = name;
		this.start = start;
		this.end = end;
		this.step = step;
	}

	public Value getStart() {
		return start;
	}

	public void setStart(Value start) {
		this.start = start;
	}

	public Value getEnd() {
		return end;
	}

	public void setEnd(Value end) {
		this.end = end;
	}

	public Value getStep() {
		return step;
	}

	public void setStep(Value step) {
		this.step = step;
	}

	@Override
	public String getName() {
		return name;
	}
}
