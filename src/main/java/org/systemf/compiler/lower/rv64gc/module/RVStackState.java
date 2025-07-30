package org.systemf.compiler.lower.rv64gc.module;

import org.systemf.compiler.lower.rv64gc.value.RVFramePointer;

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
		var mod = curSize % alignment;
		if (mod == 0) return;
		curSize += alignment - mod;
	}

	public long getSize() {
		return curSize;
	}
}