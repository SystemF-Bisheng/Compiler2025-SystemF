package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.ir.Module;

public interface OptPass {
	boolean run(Module module);
}