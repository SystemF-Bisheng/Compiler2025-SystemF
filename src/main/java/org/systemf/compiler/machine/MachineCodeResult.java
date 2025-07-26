package org.systemf.compiler.machine;

import org.systemf.compiler.machine.riscv.MachineModule;

public record MachineCodeResult(String code) {
    MachineCodeResult(MachineModule module) {
        this(module.render());
    }
}