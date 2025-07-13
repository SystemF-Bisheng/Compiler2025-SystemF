package org.systemf.compiler.semantic.type;

public interface SysYNumeric extends SysYType {
	/**
	 * @param other Another numeric type
	 * @return Return true if this is narrower than other, false otherwise
	 */
	boolean compare(SysYNumeric other);
}