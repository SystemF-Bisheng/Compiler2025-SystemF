package org.systemf.compiler.ir.value.instruction.terminal;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.type.Void;

public class Br extends Terminal {
	public final BasicBlock target;

	public Br(BasicBlock target) {
		super(Void.getInstance(), "");
		this.target = target;
	}
}