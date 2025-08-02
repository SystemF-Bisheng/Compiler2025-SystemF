package org.systemf.compiler.lower.rv64gc.module.stack;

import org.systemf.compiler.lower.rv64gc.module.register.RVFramePointer;
import org.systemf.compiler.util.MathUtil;

public class RVStackState {
	public final RVFramePointer fp = new RVFramePointer();
	private long curSize = 0;

	public long allocate(long size, long alignment) {
		pad(alignment);
		var res = curSize;
		curSize += size;
		return res;
	}

	public void pad(long alignment) {
		curSize = MathUtil.roundTo(curSize, alignment);
	}

	public long getSize() {
		return curSize;
	}
}