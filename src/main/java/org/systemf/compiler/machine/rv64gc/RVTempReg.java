package org.systemf.compiler.machine.rv64gc;

import org.systemf.compiler.lower.rv64gc.module.position.RVRegister;

public class RVTempReg {
	public final RVRegister pos;
	public boolean dirty = false;
	public boolean locked = false;
	public RVTypedPosition cached = null;

	public RVTempReg(RVRegister pos) {
		this.pos = pos;
	}

	public void invalidate(RVAsmCode out) {
		if (dirty) {
			cached.store(pos, out);
			dirty = false;
		}
		cached = null;
	}

	public void clear() {
		dirty = false;
		cached = null;
	}
}
