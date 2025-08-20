package org.systemf.compiler.optimization.pass.util;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.util.Tree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CodeMotionHelper {
	public static boolean checkNonMobile(Module module, Instruction inst) {
		return inst instanceof Terminal || ValueUtil.sideEffect(module, inst) || ValueUtil.blockSensitive(module, inst);
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

	public static BasicBlock findBestLower(BasicBlock block, BasicBlock lowerBound, Predicate<BasicBlock> lowerFilter,
			Tree<BasicBlock> domTree,
			Map<BasicBlock, Integer> frequency) {
		var possibleLower = new ArrayList<BasicBlock>();
		while (true) {
			if (lowerBound == null) break;
			if (lowerFilter.test(lowerBound)) possibleLower.add(lowerBound);
			if (lowerBound == block) break;
			lowerBound = domTree.getParent(lowerBound);
		}
		if (possibleLower.isEmpty()) return block;
		return possibleLower.stream().min(Comparator.comparingInt(frequency::get))
				.orElseThrow();
	}

	public static BasicBlock findBestLower(BasicBlock block, BasicBlock lowerBound, Tree<BasicBlock> domTree,
			Map<BasicBlock, Integer> frequency) {
		return findBestLower(block, lowerBound, _ -> true, domTree, frequency);
	}

	public static void insertHead(BasicBlock target, Instruction... inst) {
		for (var iterLower = target.instructions.listIterator(); iterLower.hasNext(); ) {
			if (iterLower.next() instanceof Phi) continue;
			iterLower.previous();
			for (var in : inst) iterLower.add(in);
			break;
		}
	}

	public static void insertEntry(Function func, Instruction... newInst) {
		CodeMotionHelper.insertHead(func.getEntryBlock(), newInst);
	}

	public static void insertTail(BasicBlock target, Instruction inst) {
		var instList = target.instructions;
		var term = instList.removeLast();
		instList.addLast(inst);
		instList.addLast(term);
	}

	public static Instruction insertBefore(ListIterator<Instruction> iterator, Instruction... newInst) {
		iterator.previous();
		for (var inst : newInst) iterator.add(inst);
		iterator.next();
		return newInst[newInst.length - 1];
	}
}
