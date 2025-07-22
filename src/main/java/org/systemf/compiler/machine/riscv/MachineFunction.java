package org.systemf.compiler.machine.riscv;

import java.util.List;

public class MachineFunction {
    private final String name;
    private final List<MachineBasicBlock> basicBlocks;
    private int stackFrameSize = 0;

    public MachineFunction(String name, List<MachineBasicBlock> basicBlocks) {
        this.name = name;
        this.basicBlocks = basicBlocks;
    }

    public MachineFunction(String name) {
        this.name = name;
        this.basicBlocks = List.of();
    }

    public String getName() {
        return name;
    }

    public void addBasicBlock(MachineBasicBlock block) {
        basicBlocks.add(block);
    }

    public List<MachineBasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public void setStackFrameSize(int size) {
        if (size % 16 != 0) {
            this.stackFrameSize = (size / 16 + 1) * 16;
        } else {
            this.stackFrameSize = size;
        }
    }

    public int getStackFrameSize() {
        return stackFrameSize;
    }

    // Use after: 
    // + StackFrameSize is set
    // + Prologue and Epilogue have been added to the basic blocks
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("\t.globl ").append(name).append("\n");
        sb.append("\t.type ").append(name).append(", @function\n");
        sb.append(name).append(":\n");
        for (MachineBasicBlock block : basicBlocks) {
            sb.append(block.render());
        }
        // sb.append("\t.size ").append(name).append(", .-").append(name).append("\n");
        // sb.append("\n");
        return sb.toString();
    }
}
