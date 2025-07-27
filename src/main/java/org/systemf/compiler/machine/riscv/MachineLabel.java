package org.systemf.compiler.machine.riscv;

public record MachineLabel(String label) implements MachineOperand {
	@Override
	public String toString() {
		return label;
	}
}
