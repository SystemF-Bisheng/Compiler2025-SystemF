package org.systemf.compiler.machine.riscv;

import java.util.List;

public class MachineModule {
    private final String name;
    private final List<MachineFunction> functions;
    private final List<MachineGlobalVariable> globalVariables;

    public MachineModule(String name, List<MachineFunction> functions) {
        this.name = name;
        this.functions = functions;
        this.globalVariables = List.of();
    }

    public MachineModule(String name) {
        this.name = name;
        this.functions = List.of();
        this.globalVariables = List.of();
    }

    public String getName() {
        return name;
    }

    public List<MachineFunction> getFunctions() {
        return functions;
    }

    public void clearFunctions() {
        functions.clear();
    }

    public void addFunction(MachineFunction function) {
        functions.add(function);
    }

    public List<MachineGlobalVariable> getGlobalVariables() {
        return globalVariables;
    }

    public void clearGlobalVariables() {
        globalVariables.clear();
    }

    public void addGlobalVariable(MachineGlobalVariable variable) {
        globalVariables.add(variable);
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        // TODO
        return sb.toString();
    }
}
