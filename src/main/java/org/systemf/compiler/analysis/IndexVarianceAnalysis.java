package org.systemf.compiler.analysis;

import org.systemf.compiler.analysis.util.BelongingHelper;
import org.systemf.compiler.ir.AllocationSite;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.type.interfaces.Indexable;
import org.systemf.compiler.ir.type.interfaces.Type;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.GetPtr;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.query.AttributeProvider;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.SaturationArithmetic;

import java.util.*;

/**
 * Depend on: PointerAnalysis, FrequencyAnalysis
 * <p>
 * Applicable to: IR
 */
public enum IndexVarianceAnalysis implements AttributeProvider<Module, IndexVarianceAnalysisResult> {
	INSTANCE;

	@Override
	public IndexVarianceAnalysisResult getAttribute(Module entity) {
		return new IndexVarianceAnalysisContext(entity).run();
	}

	private static class IndexVarianceAnalysisContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private final PointerAnalysisResult ptrResult;
		private final Map<AllocationSite, List<Integer>> variance = new HashMap<>();
		private final Map<BasicBlock, Integer> frequency = new HashMap<>();
		private final Map<Instruction, BasicBlock> belonging;

		public IndexVarianceAnalysisContext(Module module) {
			this.module = module;
			this.ptrResult = query.getAttribute(module, PointerAnalysisResult.class);
			this.belonging = BelongingHelper.getBlockBelonging(module);
		}

		private void collectFrequency(Function function) {
			var freqRes = query.getAttribute(function, FrequencyAnalysisResult.class);
			frequency.putAll(freqRes.frequency());
		}

		private void collectFrequency() {
			module.getFunctions().values().forEach(this::collectFrequency);
		}

		private int checkDim(Type type) {
			if (type instanceof Indexable indexable) return 1 + checkDim(indexable.getElementType());
			return 0;
		}

		private void collectAllocSite(AllocationSite allocSite) {
			var inner = allocSite.valueType();
			var dim = checkDim(inner);
			if (dim == 0) return;
			var thisVariance = new ArrayList<>(Collections.nCopies(dim, 0));
			var pointedBy = ptrResult.pointedBy(allocSite);
			if (!pointedBy.stream().allMatch(ptr -> ptrResult.pointTo(ptr).size() == 1)) return;
			if (!pointedBy.stream().flatMap(ptr -> ptr.getDependant().stream()).allMatch(inst ->
					inst instanceof GetPtr || inst instanceof Load || inst instanceof Store))
				return;
			pointedBy.stream().flatMap(ptr -> ptr.getDependant().stream()).filter(inst -> inst instanceof GetPtr)
					.map(inst -> (GetPtr) inst).distinct().forEach(getPtr -> {
						if (!(getPtr.getIndex() instanceof Instruction indexInst)) return;
						var afterDim = checkDim(getPtr.getType().getElementType());
						var indexPos = dim - afterDim - 1;
						var indexFreq = frequency.get(belonging.get(indexInst));
						thisVariance.set(indexPos, SaturationArithmetic.saturatedAdd(thisVariance.get(indexPos), indexFreq));
					});
			variance.put(allocSite, thisVariance);
		}

		private void collectAllocSite() {
			ptrResult.pointed().keySet().forEach(this::collectAllocSite);
		}

		public IndexVarianceAnalysisResult run() {
			collectFrequency();
			collectAllocSite();
			return new IndexVarianceAnalysisResult(variance);
		}
	}
}
