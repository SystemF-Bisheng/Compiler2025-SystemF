package org.systemf.compiler.machine.riscv;

public sealed interface MachineOperand {
	String toString();
}

record MachineRegister(String name) implements MachineOperand {
	@Override
	public String toString() {
		return name;
	}

	public static final MachineRegister ZERO = new MachineRegister("zero");
	public static final MachineRegister RA = new MachineRegister("ra");
	public static final MachineRegister SP = new MachineRegister("sp");
	public static final MachineRegister A0 = new MachineRegister("a0");
}

record MachineImmediate(int value) implements MachineOperand {
	@Override
	public String toString() {
		return String.valueOf(value);
	}
}

record MachineLabel(String label) implements MachineOperand {
	@Override
	public String toString() {
		return label;
	}
}

record MachineAddress(MachineRegister base, MachineImmediate offset) implements MachineOperand {
	@Override
	public String toString() {
		return String.format("%d(%s)", offset.value(), base.name());
	}
}