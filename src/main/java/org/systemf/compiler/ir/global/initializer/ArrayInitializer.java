package org.systemf.compiler.ir.global.initializer;

public record ArrayInitializer(int length, IGlobalInitializer... elements) implements IGlobalInitializer {
}