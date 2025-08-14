package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.ConstantInt;

import java.util.List;

public record SimpleForAnalysisResult(List<SimpleFor> loops) {
	public record SimpleFor(Value begin, Value end, ConstantInt step, boolean inclusive, BasicBlock head,
	                        BasicBlock body) {
	}
}
