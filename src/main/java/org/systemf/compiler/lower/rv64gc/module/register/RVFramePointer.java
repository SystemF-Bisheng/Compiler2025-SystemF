package org.systemf.compiler.lower.rv64gc.module.register;

import org.systemf.compiler.ir.type.I64;
import org.systemf.compiler.lower.rv64gc.module.position.RVRegister;
import org.systemf.compiler.lower.rv64gc.util.RVRegUtil;

public class RVFramePointer extends RVBuiltInRegister {
	public RVFramePointer() {
		super(I64.INSTANCE);
	}

	@Override
	public String toString() {
		return "fp";
	}

	@Override
	public RVRegister position() {
		return RVRegUtil.FRAME_POINTER;
	}
}
