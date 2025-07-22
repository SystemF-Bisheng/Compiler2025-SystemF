package org.systemf.compiler.machine.riscv;

// import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MachineInstruction {
    private final String opcode;
    private final List<MachineOperand> operands;

    public MachineInstruction(String opcode, List<MachineOperand> operands) {
        this.opcode = opcode;
        this.operands = operands;
    }

    public MachineInstruction(String opcode, MachineOperand... operands) {
        this(opcode, List.of(operands));
    }

    public String getOpcode() {
        return opcode;
    }

    public List<MachineOperand> getOperands() {
        return operands;
    }

    public String render() {
        String renderedOperands = operands.stream()
                .map(MachineOperand::toString)
                .collect(Collectors.joining(", "));
        return String.format("\t%s %s", opcode, renderedOperands);
    }
}
