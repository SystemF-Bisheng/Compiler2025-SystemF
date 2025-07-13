package org.systemf.compiler.semantic;

public class IllegalSemanticException extends RuntimeException {
	public IllegalSemanticException(String message) {
		super(message);
	}

	public IllegalSemanticException(String message, Throwable cause) {
		super(message, cause);
	}
}