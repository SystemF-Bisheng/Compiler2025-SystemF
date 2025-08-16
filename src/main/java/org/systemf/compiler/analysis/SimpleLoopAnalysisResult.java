package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;

import java.util.List;

public record SimpleLoopAnalysisResult(List<SimpleLoop> loops) {
	public record SimpleLoop(BasicBlock head,
	                         BasicBlock body,
	                         BasicBlock merge) {
	}
}
