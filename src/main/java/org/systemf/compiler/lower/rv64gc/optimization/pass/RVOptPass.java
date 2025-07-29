package org.systemf.compiler.lower.rv64gc.optimization.pass;

import org.systemf.compiler.lower.rv64gc.module.RVModule;

public interface RVOptPass {
	boolean run(RVModule rvModule);
}
