package org.systemf.compiler.optimization.pass.util;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.util.Tree;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class CodeMotionHelper {
	public static Map<Instruction, BasicBlock> getBelonging(Function function) {
		var res = new HashMap<Instruction, BasicBlock>();
		function.getBlocks().forEach(block -> block.instructions.forEach(inst -> res.put(inst, block)));
		return res;
	}

	public static BasicBlock getUpperBound(Instruction instruction, Tree<BasicBlock> domTree,
			Map<Instruction, BasicBlock> belonging) {
		return instruction.getDependency().stream().filter(dep -> dep instanceof Value && dep instanceof Instruction)
				.map(dep -> (Instruction) dep).map(belonging::get).max(Comparator.comparingInt(domTree::getDfn))
				.orElse(domTree.getRoot());
	}

	public static BasicBlock getLowerBound(Instruction instruction, Tree<BasicBlock> domTree,
			Map<Instruction, BasicBlock> belonging) {
		if (!(instruction instanceof Value val)) return null;
		return val.getDependant().stream().map(belonging::get).reduce(domTree::lca).orElse(null);
	}
}
