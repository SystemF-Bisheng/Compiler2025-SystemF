package org.systemf.compiler.lower.rv64gc.analysis;

import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.lower.rv64gc.instruction.RVParallelMove;
import org.systemf.compiler.query.AttributeProvider;
import org.systemf.compiler.query.QueryManager;

import java.util.*;

public enum RVLiveRangeAnalysis implements AttributeProvider<Function, RVLiveRangeAnalysisResult> {
	INSTANCE;

	@Override
	public RVLiveRangeAnalysisResult getAttribute(Function entity) {
		return new RVLiveRangeAnalysisContext(entity).run();
	}

	private static class RVLiveRangeAnalysisContext {
		private final Function function;
		private final RVCFGAnalysisResult cfg;
		private final Map<Instruction, Set<Value>> aliveBefore = new HashMap<>();
		private final Map<Instruction, List<Instruction>> instPred = new HashMap<>();

		public RVLiveRangeAnalysisContext(Function function) {
			this.function = function;
			this.cfg = QueryManager.getInstance().getAttribute(function, RVCFGAnalysisResult.class);
		}

		private void markAlive(Instruction inst, Value value, Instruction stopOn) {
			if (inst == stopOn) return;
			if (!aliveBefore.get(inst).add(value)) return;
			instPred.get(inst).forEach(pred -> markAlive(pred, value, stopOn));
		}

		private void markAlive(Value value, Instruction stopOn) {
			value.getDependant().stream().filter(inst -> {
				if (!(inst instanceof RVParallelMove parMove)) return true;
				return parMove.getMoves().containsValue(value);
			}).forEach(inst -> markAlive(inst, value, stopOn));
		}

		private void init() {
			function.allInstructions().forEach(inst -> aliveBefore.put(inst, new HashSet<>()));
			for (var block : function.getBlocks()) {
				var predEnds = cfg.predecessors(block).stream().map(BasicBlock::getTerminator)
						.map(term -> (Instruction) term).toList();
				Instruction lastInst = null;
				for (var inst : block.instructions) {
					if (lastInst == null) instPred.put(inst, predEnds);
					else instPred.put(inst, List.of(lastInst));
					lastInst = inst;
				}
			}
		}

		public RVLiveRangeAnalysisResult run() {
			init();
			for (var parameter : function.getFormalArgs()) markAlive(parameter, null);
			function.allInstructions().filter(inst -> inst instanceof Value)
					.forEach(inst -> markAlive((Value) inst, inst));
			return new RVLiveRangeAnalysisResult(aliveBefore);
		}
	}
}
