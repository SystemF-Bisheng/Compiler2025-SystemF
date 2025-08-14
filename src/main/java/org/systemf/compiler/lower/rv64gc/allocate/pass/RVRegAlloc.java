package org.systemf.compiler.lower.rv64gc.allocate.pass;

import org.systemf.compiler.analysis.FrequencyAnalysis;
import org.systemf.compiler.analysis.util.BelongingHelper;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.lower.rv64gc.analysis.RVDominanceAnalysisResult;
import org.systemf.compiler.lower.rv64gc.analysis.RVLiveRangeAnalysisResult;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.lower.rv64gc.module.position.RVPosition;
import org.systemf.compiler.lower.rv64gc.module.position.RVRegister;
import org.systemf.compiler.lower.rv64gc.module.position.RVStackPos;
import org.systemf.compiler.lower.rv64gc.module.register.RVRegisterType;
import org.systemf.compiler.lower.rv64gc.module.stack.RVStackState;
import org.systemf.compiler.lower.rv64gc.util.RVRegUtil;
import org.systemf.compiler.lower.rv64gc.util.RVTypeHelper;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.SaturationArithmetic;

import java.util.*;

public enum RVRegAlloc {
	INSTANCE;

	public void run(RVModule module) {
		new RVRegAllocContext(module).run();
	}

	private static class RVRegAllocContext {
		private static final int NON_SAVED_PRIORITY_THRESHOLD = FrequencyAnalysis.BASE_FREQUENCY * 4;
		private final QueryManager query = QueryManager.getInstance();
		private final Map<Value, RVPosition> position;
		private final Map<Function, RVStackState> stacks;
		private final Map<BasicBlock, Integer> frequency;
		private final Module module;
		private Map<Instruction, BasicBlock> belonging;
		private SequencedMap<Value, Integer> colorMap;
		private Map<Value, Integer> spillCost;
		private Map<Value, Integer> saveCost;
		private RVLiveRangeAnalysisResult liveRange;

		public RVRegAllocContext(RVModule rvModule) {
			this.frequency = rvModule.frequency();
			this.stacks = rvModule.stacks();
			this.module = rvModule.module();
			this.position = rvModule.position();
		}

		private void colorValue(Value val) {
			var aliveBefore = liveRange.aliveBeforeInst(val);
			if (aliveBefore.isEmpty()) return;
			var regType = RVRegUtil.regType(val);
			var curColor = aliveBefore.stream().flatMap(live -> liveRange.aliveBefore(live).stream()).distinct()
					.filter(other -> regType == RVRegUtil.regType(other)).filter(colorMap::containsKey)
					.mapToInt(colorMap::get).sorted().reduce(0, (mex, x) -> mex == x ? mex + 1 : mex);
			colorMap.put(val, curColor);
			var sCost = val.getDependant().stream().map(belonging::get).map(frequency::get)
					.reduce(0, SaturationArithmetic::saturatedAdd);
			spillCost.put(val, sCost);
			var cCost = SaturationArithmetic.saturatedMul(
					aliveBefore.stream().filter(live -> live instanceof AbstractCall)
							.filter(live -> liveRange.aliveAfter(live).contains(val)).map(belonging::get)
							.map(frequency::get).reduce(0, SaturationArithmetic::saturatedAdd), 2);
			saveCost.put(val, cCost);
		}

		private void colorBlock(BasicBlock block) {
			for (var inst : block.instructions) {
				if (!(inst instanceof Value val)) continue;
				colorValue(val);
			}
		}

		private void colorFunction(Function function) {
			this.belonging = BelongingHelper.getBelonging(function);
			this.colorMap = new LinkedHashMap<>();
			this.spillCost = new HashMap<>();
			this.saveCost = new HashMap<>();
			this.liveRange = query.getAttribute(function, RVLiveRangeAnalysisResult.class);

			for (var param : function.getFormalArgs()) colorValue(param);
			var domTree = query.getAttribute(function, RVDominanceAnalysisResult.class).dominance();
			function.getBlocks().stream().sorted(Comparator.comparingInt(domTree::getDfn)).forEach(this::colorBlock);
		}

		private void allocColor(Function function, RVRegisterType regType) {
			var stack = stacks.get(function);
			var spillCostSum = new HashMap<Integer, Integer>();
			var saveCostSum = new HashMap<Integer, Integer>();
			var colorVals = new TreeMap<Integer, List<Value>>();
			colorMap.entrySet().stream().filter(entry -> regType == RVRegUtil.regType(entry.getKey()))
					.forEach(entry -> {
						var val = entry.getKey();
						var sCost = spillCost.get(val);
						var cCost = saveCost.get(val);
						var color = entry.getValue();
						spillCostSum.compute(color,
								(_, sum) -> sum == null ? sCost : SaturationArithmetic.saturatedAdd(sum, sCost));
						saveCostSum.compute(color,
								(_, sum) -> sum == null ? cCost : SaturationArithmetic.saturatedAdd(sum, cCost));
						colorVals.computeIfAbsent(color, _ -> new ArrayList<>()).add(val);
					});

			var leftSaved = new TreeSet<>(RVRegUtil.AVAILABLE_SAVED.get(regType));
			var leftNonSaved = new TreeSet<>(RVRegUtil.AVAILABLE_NON_SAVED.get(regType));
			while (!colorVals.isEmpty()) {
				RVPosition pos;
				int color;
				if (!leftNonSaved.isEmpty() &&
				    colorVals.keySet().stream().anyMatch(col -> saveCostSum.get(col) <= NON_SAVED_PRIORITY_THRESHOLD)) {
					var regIndex = leftNonSaved.getFirst();
					leftNonSaved.remove(regIndex);
					pos = new RVRegister(regType, regIndex);
					color = colorVals.keySet().stream()
							.filter(col -> saveCostSum.get(col) <= NON_SAVED_PRIORITY_THRESHOLD)
							.max(Comparator.comparingInt(spillCostSum::get)).orElseThrow();
				} else if (!leftSaved.isEmpty()) {
					var regIndex = leftSaved.getFirst();
					leftSaved.remove(regIndex);
					pos = new RVRegister(regType, regIndex);
					color = colorVals.keySet().stream().max(Comparator.comparingInt(
							curColor -> SaturationArithmetic.saturatedAdd(spillCostSum.get(curColor),
									saveCostSum.get(curColor)))).orElseThrow();
				} else if (!leftNonSaved.isEmpty()) {
					var regIndex = leftNonSaved.getFirst();
					leftNonSaved.remove(regIndex);
					pos = new RVRegister(regType, regIndex);
					color = colorVals.keySet().stream().max(Comparator.comparingInt(spillCostSum::get)).orElseThrow();
				} else {
					var entry = colorVals.firstEntry();
					color = entry.getKey();
					long size = 0, alignment = 0;
					for (var val : entry.getValue()) {
						var type = val.getType();
						size = Math.max(size, RVTypeHelper.sizeOf((Sized) type));
						alignment = Math.max(alignment, RVTypeHelper.alignmentOf((Sized) type));
					}
					pos = new RVStackPos(stack.allocate(size, alignment));
				}
				var vals = colorVals.get(color);
				colorVals.remove(color);
				spillCostSum.remove(color);
				saveCostSum.remove(color);
				for (var val : vals) position.put(val, pos);
			}
		}

		private void allocColor(Function function) {
			for (var type : RVRegisterType.values()) allocColor(function, type);
		}

		private void alloc() {
			for (var func : module.getFunctions().values()) {
				colorFunction(func);
				allocColor(func);
			}
		}

		public void run() {
			alloc();
		}
	}
}
