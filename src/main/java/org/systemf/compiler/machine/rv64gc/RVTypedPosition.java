package org.systemf.compiler.machine.rv64gc;

import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.lower.rv64gc.module.position.RVPosition;
import org.systemf.compiler.lower.rv64gc.module.position.RVRegister;
import org.systemf.compiler.lower.rv64gc.module.position.RVStackPos;
import org.systemf.compiler.lower.rv64gc.util.RVRegUtil;

public record RVTypedPosition(Sized type, RVPosition position) {
	public RVTypedPosition with(RVPosition newPosition) {
		return new RVTypedPosition(type, newPosition);
	}

	/**
	 * Direct load that doesn't use cache
	 */
	public void load(RVRegister dest, RVAsmCode out) {
		switch (position) {
			case RVRegister src -> RVGenerateHelper.moveRegister(dest, src, out);
			case RVStackPos(long offset) -> {
				var loadInst = RVGenerateHelper.loadInstruction(type);
				RVGenerateHelper.performOffset(loadInst, RVRegUtil.FRAME_POINTER, dest, offset, out);
			}
		}
	}

	/**
	 * Direct store that doesn't use cache
	 */
	public void store(RVRegister src, RVAsmCode out) {
		switch (position) {
			case RVRegister dest -> RVGenerateHelper.moveRegister(dest, src, out);
			case RVStackPos(long offset) -> {
				var storeInst = RVGenerateHelper.storeInstruction(type);
				RVGenerateHelper.performOffset(storeInst, RVRegUtil.FRAME_POINTER, src, offset, out);
			}
		}
	}
}
