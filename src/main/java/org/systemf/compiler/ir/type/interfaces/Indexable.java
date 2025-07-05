package org.systemf.compiler.ir.type.interfaces;

public interface Indexable extends Dereferenceable {
	Sized getElementType();
}