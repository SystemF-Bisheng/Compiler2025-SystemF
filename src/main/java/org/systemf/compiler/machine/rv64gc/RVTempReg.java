package org.systemf.compiler.machine.rv64gc;

import org.systemf.compiler.lower.rv64gc.module.position.RVRegister;

public class RVTempReg {
	public final RVRegister pos;
	public boolean dirty = false;
	public boolean used = false;
	public RVTypedPosition cached = null;
	private boolean locked = false;

	public RVTempReg(RVRegister pos) {
		this.pos = pos;
	}

	public void lock() {
		locked = true;
		used = true;
	}

	public void unlock() {
		locked = false;
	}

	public void invalidate(RVAsmCode out) {
		if (dirty) {
			cached.store(pos, out);
			dirty = false;
		}
		used = false;
		cached = null;
	}

	public void clear() {
		dirty = false;
		used = false;
		cached = null;
	}

	public boolean isLocked() {
		return locked;
	}
}
