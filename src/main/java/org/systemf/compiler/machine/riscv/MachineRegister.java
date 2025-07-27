package org.systemf.compiler.machine.riscv;

public record MachineRegister(String name) implements MachineOperand {
	@Override
	public String toString() {
		return name;
	}

	public static final MachineRegister ZERO = new MachineRegister("zero");
	public static final MachineRegister RA = new MachineRegister("ra");
	public static final MachineRegister SP = new MachineRegister("sp");
	public static final MachineRegister FP = new MachineRegister("fp");
	public static final MachineRegister A0 = new MachineRegister("a0");
	public static final MachineRegister A1 = new MachineRegister("a1");
}