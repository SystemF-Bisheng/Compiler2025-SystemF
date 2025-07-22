package org.systemf.compiler.machine.riscv;

import java.util.List;

public class MachineBasicBlock {
    private final String label;
    private final List<MachineInstruction> instructions;

    public MachineBasicBlock(String label, List<MachineInstruction> instructions) {
        this.label = label;
        this.instructions = instructions;
    }

    public MachineBasicBlock(String label) {
        this.label = label;
        this.instructions = List.of();
    }

    public void addInstruction(MachineInstruction instruction) {
        instructions.add(instruction);
    }

    public String getLabel() {
        return label;
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append(label).append(":\n");
        for (MachineInstruction instruction : instructions) {
            sb.append(instruction.render()).append("\n");
        }
        return sb.toString();
    }
}
