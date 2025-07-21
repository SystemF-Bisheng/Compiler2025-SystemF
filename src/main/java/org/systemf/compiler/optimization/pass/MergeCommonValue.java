package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.DominanceAnalysisResult;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.PotentialNonRepeatable;
import org.systemf.compiler.ir.value.instruction.PotentialSideEffect;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public enum MergeCommonValue implements OptPass {
	INSTANCE;

	private boolean mergeValues(DominanceAnalysisResult dom, List<Pair<PositionInfo, Value>> values) {
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

	private boolean handleValues(DominanceAnalysisResult dom, List<Pair<PositionInfo, Value>> values) {
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

	private boolean processFunction(Function function) {
		var query = QueryManager.getInstance();
		var valueMap = new HashMap<Class<?>, List<Pair<PositionInfo, Value>>>();
		var dom = query.getAttribute(function, DominanceAnalysisResult.class);

		for (var block : function.getBlocks()) {
			var index = 0;
			for (var inst : block.instructions) {
				if (!(inst instanceof Value val)) continue;
				if (val instanceof PotentialSideEffect || val instanceof PotentialNonRepeatable) continue;
				valueMap.computeIfAbsent(val.getClass(), _ -> new LinkedList<>())
						.add(Pair.of(new PositionInfo(block, index), val));
				++index;
			}
		}

		var res = valueMap.values().stream().map(values -> handleValues(dom, values)).reduce(false, (a, b) -> a || b);

		if (res) query.invalidateAllAttributes(function);
		return res;
	}

	@Override
	public boolean run(Module module) {
		var res = module.getFunctions().values().stream().map(this::processFunction).reduce(false, (a, b) -> a || b);
		if (res) QueryManager.getInstance().invalidateAllAttributes(module);
		return res;
	}

	private record PositionInfo(BasicBlock block, int index) {
	}
}
