package org.systemf.compiler.optimization.pass.util;

import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.IRCloner;
import org.systemf.compiler.ir.ITracked;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.value.Value;

import java.util.Map;

public class CodeGenHelper {
	public static void cloneBlock(IRBuilder builder, BasicBlock oldBlock, BasicBlock newBlock,
			Map<ITracked, ITracked> substitute) {
		var cloner = new IRCloner(builder);
		builder.attachToBlockTail(newBlock);
		substitute.put(oldBlock, newBlock);
		for (var inst : oldBlock.instructions) {
			var newInst = inst.accept(cloner);
			if (inst instanceof Value oldValue) substitute.put(oldValue, (Value) newInst);
		}
	}

	public static void replaceAll(BasicBlock block, Map<? extends ITracked, ? extends ITracked> substitute) {
		for (var inst : block.instructions)
			inst.getDependency().stream().filter(substitute::containsKey).forEach(oldDep -> {
				var newDep = substitute.get(oldDep);
				inst.replaceAll(oldDep, newDep);
			});
	}

	public static void replaceAll(BasicBlock block, ITracked oldOne, ITracked newOne) {
		replaceAll(block, Map.of(oldOne, newOne));
	}
}
