package org.systemf.compiler.ir.global.initializer;

import org.systemf.compiler.ir.value.Value;

public record AtomicInitializer(Value value) implements IGlobalInitializer {
}