package org.systemf.compiler.optimization.pass.util;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.util.Tree;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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
		var lower = val.getDependant().stream().flatMap(inst -> {
			if (inst instanceof Phi phi)
				return phi.getIncoming().entrySet().stream().filter(entry -> entry.getValue() == val)
						.map(Map.Entry::getKey);
			else return Stream.of(belonging.get(inst));
		}).reduce(domTree::lca);
		return lower.orElse(null);
	}

	public static void insertHead(BasicBlock target, Instruction inst) {
		for (var iterLower = target.instructions.listIterator(); iterLower.hasNext(); ) {
			if (iterLower.next() instanceof Phi) continue;
			iterLower.previous();
			iterLower.add(inst);
			break;
		}
	}

	public static void insertTail(BasicBlock target, Instruction inst) {
		var instList = target.instructions;
		var term = instList.removeLast();
		instList.addLast(inst);
		instList.addLast(term);
	}
}
