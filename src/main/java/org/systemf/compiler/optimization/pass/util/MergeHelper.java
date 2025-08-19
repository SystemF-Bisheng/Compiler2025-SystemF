package org.systemf.compiler.optimization.pass.util;

import org.systemf.compiler.analysis.CFGAnalysisResult;
import org.systemf.compiler.analysis.PointerAnalysisResult;
import org.systemf.compiler.analysis.ReachabilityAnalysisResult;
import org.systemf.compiler.ir.AllocationSite;
import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.DummyBinary;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.instruction.terminal.Terminal;
import org.systemf.compiler.ir.value.util.ValueUtil;
import org.systemf.compiler.util.Pair;
import org.systemf.compiler.util.SaturationArithmetic;
import org.systemf.compiler.util.Tree;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MergeHelper {
	public static boolean mergeValues(Tree<BasicBlock> domTree, List<Pair<PositionInfo, Value>> values) {
		BasicBlock lastBlock = null;
		Value lastValue = null;
		values.sort((a, b) -> a.left().compare(b.left(), domTree));
		var res = false;
		for (var valInfo : values) {
			var pos = valInfo.left();
			var block = pos.block();
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

	public static boolean mergeValuesInBlock(List<Pair<Integer, Value>> values) {
		values.sort(Comparator.comparingInt(Pair::left));
		var res = false;
		Value lastValue = null;
		for (var valInfo : values) {
			var val = valInfo.right();
			if (lastValue != null) {
				val.replaceAllUsage(lastValue);
				res = true;
			} else lastValue = val;
		}
		return res;
	}

	public static boolean handleValuesInBlock(List<Pair<Integer, Value>> values) {
		boolean res = false;
		while (!values.isEmpty()) {
			var iter = values.iterator();
			var val = iter.next();
			iter.remove();
			var toMerge = new ArrayList<Pair<Integer, Value>>();
			toMerge.add(val);
			while (iter.hasNext()) {
				var next = iter.next();
				if (!val.right().contentEqual(next.right())) continue;
				iter.remove();
				toMerge.add(next);
			}
			res |= mergeValuesInBlock(toMerge);
		}
		return res;
	}

	public static boolean handleValuesInBlock(Map<Class<?>, List<Pair<Integer, Value>>> valueMap) {
		return valueMap.values().stream().map(MergeHelper::handleValuesInBlock).reduce(false, (a, b) -> a || b);
	}

	public static boolean handleInBlock(BasicBlock block, Predicate<Value> valueFilter) {
		var valueMap = new HashMap<Class<?>, List<Pair<Integer, Value>>>();

		var index = 0;
		for (var inst : block.instructions) {
			if (!(inst instanceof Value val)) continue;
			if (!valueFilter.test(val)) continue;
			valueMap.computeIfAbsent(val.getClass(), _ -> new LinkedList<>()).add(Pair.of(index, val));
			++index;
		}

		return handleValuesInBlock(valueMap);
	}

	public static boolean handleValues(Tree<BasicBlock> domTree, List<Pair<PositionInfo, Value>> values) {
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
			res |= mergeValues(domTree, toMerge);
		}
		return res;
	}

	public static boolean handleValues(Tree<BasicBlock> domTree,
			Map<Class<?>, List<Pair<PositionInfo, Value>>> valueMap) {
		return valueMap.values().stream().map(values -> handleValues(domTree, values)).reduce(false, (a, b) -> a || b);
	}

	public static boolean handleFunction(Function function, Tree<BasicBlock> domTree, Predicate<Value> valueFilter) {
		var valueMap = new HashMap<Class<?>, List<Pair<PositionInfo, Value>>>();

		for (var block : function.getBlocks()) {
			var index = 0;
			for (var inst : block.instructions) {
				if (!(inst instanceof Value val)) continue;
				if (!valueFilter.test(val)) continue;
				valueMap.computeIfAbsent(val.getClass(), _ -> new LinkedList<>())
						.add(Pair.of(new PositionInfo(block, index), val));
				++index;
			}
		}

		return handleValues(domTree, valueMap);
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
		var loadMap = new LinkedHashMap<Value, Value>();
		for (var inst : block.instructions) manipulateLoadMap(inst, loadMap, module, ptrResult);
		return loadMap;
	}

	public static void manipulateStoreSet(Instruction inst, Set<Value> storeSet, Module module,
			PointerAnalysisResult ptrResult) {
		if (inst instanceof Load load) {
			var ptr = load.getPointer();
			var affected = ptrResult.pointTo(ptr);
			for (var iter = storeSet.iterator(); iter.hasNext(); ) {
				var storePtr = iter.next();
				var related = ptrResult.pointTo(storePtr);
				if (affected.stream().anyMatch(related::contains)) iter.remove();
			}
		} else if (inst instanceof Store store) storeSet.add(store.getDest());
		else if (ValueUtil.sideEffect(module, inst)) storeSet.clear();
	}

	public static Set<Value> constructStoreSet(BasicBlock block, Module module, PointerAnalysisResult ptrResult) {
		var storeSet = new HashSet<Value>();
		for (var inst : block.instructions.reversed()) manipulateStoreSet(inst, storeSet, module, ptrResult);
		return storeSet;
	}

	public static boolean mergeIntBinary(IRBuilder builder, DummyBinary inst,
			BiFunction<Long, Long, Optional<Long>> func) {
		var x = inst.getX();
		var selfY = inst.getY();
		if (!ValueUtil.isConstantInt(selfY)) return false;
		if (inst.getClass() != x.getClass()) return false;
		var binaryX = (DummyBinary) x;
		var otherY = binaryX.getY();
		if (!ValueUtil.isConstantInt(otherY)) return false;
		var width = ValueUtil.getWidth(inst);
		var newValueOpt = func.apply(ValueUtil.getConstantInt(otherY), ValueUtil.getConstantInt(selfY));
		if (newValueOpt.isEmpty()) return false;
		long newValue = newValueOpt.get();
		if (SaturationArithmetic.isOverflow(newValue, width)) return false;
		inst.setX(binaryX.getX());
		inst.setY(builder.buildConstantInt(newValue, width));
		return true;
	}

	public static boolean manipulateAffected(Module module, Instruction inst, Set<Value> affected,
			PointerAnalysisResult ptrResult) {
		return switch (inst) {
			case Load _, Terminal _ -> false;
			case Store store -> {
				affected.addAll(ptrResult.pointTo(store.getDest()));
				yield false;
			}
			default -> ValueUtil.sideEffect(module, inst);
		};
	}

	public static Map<BasicBlock, Optional<Set<Value>>> constructAffecting(Module module, Function function,
			PointerAnalysisResult ptrResult) {
		var affecting = new HashMap<BasicBlock, Optional<Set<Value>>>();
		for (var block : function.getBlocks()) {
			Set<Value> affected = new HashSet<>();
			for (var inst : block.instructions)
				if (manipulateAffected(module, inst, affected, ptrResult)) {
					affected = null;
					break;
				}
			affecting.put(block, Optional.ofNullable(affected));
		}
		return affecting;
	}

	public static boolean affectingFree(BasicBlock upper, BasicBlock block, Set<AllocationSite> loadFrom,
			Map<BasicBlock, Optional<Set<Value>>> affecting, CFGAnalysisResult cfg,
			ReachabilityAnalysisResult reachability) {
		var possibleAffect = reachability.reachable().get(upper).stream()
				.filter(succ -> succ != block)
				.filter(succ -> {
					var succAffect = affecting.get(succ);
					if (succAffect.isEmpty()) return true;
					var affectSet = succAffect.get();
					return loadFrom.stream().anyMatch(affectSet::contains);
				}).collect(Collectors.toSet());
		return !blockingReachability(cfg, possibleAffect, Collections.singleton(block),
				Collections.singleton(upper));
	}

	public static boolean manipulateRequired(Module module, Instruction inst, Set<Value> required,
			PointerAnalysisResult ptrResult) {
		return switch (inst) {
			case Store _, Terminal _ -> false;
			case Load load -> {
				required.addAll(ptrResult.pointTo(load.getPointer()));
				yield false;
			}
			default -> !ValueUtil.repeatable(module, inst);
		};
	}

	public static Map<BasicBlock, Optional<Set<Value>>> constructRequiring(Module module, Function function,
			PointerAnalysisResult ptrResult) {
		var requiring = new HashMap<BasicBlock, Optional<Set<Value>>>();
		for (var block : function.getBlocks()) {
			var required = new HashSet<Value>();
			for (var inst : block.instructions) {
				if (manipulateRequired(module, inst, required, ptrResult)) {
					required = null;
					break;
				}
			}
			requiring.put(block, Optional.ofNullable(required));
		}
		return requiring;
	}

	public static boolean requiringFree(BasicBlock block, BasicBlock lower, Set<AllocationSite> storeTo,
			Map<BasicBlock, Optional<Set<Value>>> requiring, CFGAnalysisResult cfg,
			ReachabilityAnalysisResult reachability) {
		var possibleRequire = reachability.reachable().get(block).stream()
				.filter(succ -> succ != block)
				.filter(succ -> {
					var succRequire = requiring.get(succ);
					if (succRequire.isEmpty()) return true;
					var requireSet = succRequire.get();
					return storeTo.stream().anyMatch(requireSet::contains);
				}).collect(Collectors.toSet());
		return !blockingReachability(cfg, Collections.singleton(block), possibleRequire, Collections.singleton(lower));
	}
}
