package org.systemf.compiler.ir.global.initializer;

public class ArrayInitializer implements IGlobalInitializer {
	public final int length;
	public final IGlobalInitializer[] elements;

	public ArrayInitializer(int length, IGlobalInitializer... elements) {
		this.length = length;
		this.elements = elements;
	}
}