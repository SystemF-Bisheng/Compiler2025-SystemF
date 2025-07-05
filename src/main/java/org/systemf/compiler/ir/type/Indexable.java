package org.systemf.compiler.ir.type;

public interface Indexable extends Type {
	Type getElementType();
}