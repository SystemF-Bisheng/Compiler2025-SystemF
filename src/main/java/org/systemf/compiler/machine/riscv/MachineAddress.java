package org.systemf.compiler.machine.riscv;

public record MachineAddress(MachineRegister base, MachineImmediate offset) implements MachineOperand {
	@Override
	public String toString() {
		return String.format("%d(%s)", offset.value(), base.name());
	}
}