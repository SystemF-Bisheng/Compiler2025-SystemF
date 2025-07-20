package org.systemf.compiler.interpreter.value;

public interface ExecutionValue {
	void setValue(ExecutionValue value);

	ExecutionValue clone();
}