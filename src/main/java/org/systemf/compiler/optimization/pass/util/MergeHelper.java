package org.systemf.compiler.optimization.pass.util;

import org.systemf.compiler.analysis.CFGAnalysisResult;
import org.systemf.compiler.analysis.DominanceAnalysisResult;
import org.systemf.compiler.analysis.PointerAnalysisResult;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.Pair;

import java.util.*;
import java.util.function.Predicate;

public class MergeHelper {
	public static boolean mergeValues(DominanceAnalysisResult dom, List<Pair<PositionInfo, Value>> values) {
		var domTree = dom.dominance();
		BasicBlock lastBlock = null;
		Value lastValue = null;
		values.sort((a, b) -> {
			var aPos = a.left();
			var bPos = b.left();
			if (aPos.block == bPos.block) return Integer.compare(aPos.index, bPos.index);
			return Integer.compare(domTree.getDfn(aPos.block), domTree.getDfn(bPos.block));
		});
		var res = false;
		for (var valInfo : values) {
			var pos = valInfo.left();
			var block = pos.block;
			var val = valInfo.right();
			if (lastBlock == null || !domTree.subtree(lastBlock, block)) {
				lastBlock = block;
				lastValue = val;
				continue;
			}
			val.replaceAllUsage(lastValue);
			res = true;
		}
		return res;
	}

	public static boolean handleValues(DominanceAnalysisResult dom, List<Pair<PositionInfo, Value>> values) {
		boolean res = false;
		while (!values.isEmpty()) {
			var iter = values.iterator();
			var val = iter.next();
			iter.remove();
			var toMerge = new ArrayList<Pair<PositionInfo, Value>>();
			toMerge.add(val);
			while (iter.hasNext()) {
				var next = iter.next();
				if (!val.right().contentEqual(next.right())) continue;
				iter.remove();
				toMerge.add(next);
			}
			res |= mergeValues(dom, toMerge);
		}
		return res;
	}

	public static boolean handleValues(Function function, Map<Class<?>, List<Pair<PositionInfo, Value>>> valueMap) {
		var dom = QueryManager.getInstance().getAttribute(function, DominanceAnalysisResult.class);
		return valueMap.values().stream().map(values -> handleValues(dom, values)).reduce(false, (a, b) -> a || b);
	}

	public static boolean handleFunction(Function function, Predicate<Value> valueFilter) {
		var valueMap = new HashMap<Class<?>, List<Pair<PositionInfo, Value>>>();

		for (var block : function.getBlocks()) {
			var index = 0;
			for (var inst : block.instructions) {
				if (!(inst instanceof Value val)) continue;
				if (!valueFilter.test(val)) continue;
				valueMap.computeIfAbsent(val.getClass(), _ -> new LinkedList<>())
						.add(Pair.of(new MergeHelper.PositionInfo(block, index), val));
				++index;
			}
		}

		return handleValues(function, valueMap);
	}

	public static boolean blockingReachability(CFGAnalysisResult cfg, Set<BasicBlock> begin, Set<BasicBlock> end,
			Set<BasicBlock> blocked) {
		var visited = new HashSet<>(blocked);
		var worklist = new ArrayDeque<>(begin);
		while (!worklist.isEmpty()) {
			var front = worklist.poll();
			if (visited.contains(front)) continue;
			if (end.contains(front)) return true;
			visited.add(front);
			worklist.addAll(cfg.successors(front));
		}
		return false;
	}

	public static void manipulateLoadMap(Instruction inst, Map<Value, Value> loadMap, Module module,
			PointerAnalysisResult ptrResult) {
		if (inst instanceof Store store) {
			var ptr = store.getDest();
			var affected = ptrResult.pointTo(ptr);
			for (var iter = loadMap.entrySet().iterator(); iter.hasNext(); ) {
				var entry = iter.next();
				var entryPtr = entry.getKey();
				var related = ptrResult.pointTo(entryPtr);
				if (affected.stream().anyMatch(related::contains)) iter.remove();
			}
			loadMap.put(ptr, store.getSrc());
		} else if (inst instanceof Load load) loadMap.put(load.getPointer(), load);
		else if (ValueUtil.sideEffect(module, inst)) loadMap.clear();
	}

	public static Map<Value, Value> constructLoadMap(BasicBlock block, Module module, PointerAnalysisResult ptrResult) {
		var loadMap = new HashMap<Value, Value>();
		for (var inst : block.instructions) manipulateLoadMap(inst, loadMap, module, ptrResult);
		return loadMap;
	}

	public record PositionInfo(BasicBlock block, int index) {
	}
}
