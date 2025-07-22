package org.systemf.compiler.machine;

import org.systemf.compiler.query.EntityProvider;

public enum RISCVGenerator implements EntityProvider<MachineCodeResult> {
	INSTANCE;

	@Override
	public MachineCodeResult produce() {
		// TODO: Implement RISC-V assembly generation
		throw new UnsupportedOperationException();
	}
}