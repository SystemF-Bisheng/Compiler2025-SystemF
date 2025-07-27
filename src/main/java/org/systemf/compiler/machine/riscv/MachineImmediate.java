package org.systemf.compiler.machine.riscv;

public record MachineImmediate(int value) implements MachineOperand {
	@Override
	public String toString() {
		return String.valueOf(value);
	}
}