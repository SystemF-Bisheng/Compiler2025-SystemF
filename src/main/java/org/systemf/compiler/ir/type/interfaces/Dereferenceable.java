package org.systemf.compiler.ir.type.interfaces;

public interface Dereferenceable extends Type {
	Type getElementType();
}