package org.systemf.compiler.lower.rv64gc.module;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;

import java.util.Map;

public record RVModule(Module module, Map<BasicBlock, Integer> frequency, Map<Function, RVStackState> stacks) {
}
