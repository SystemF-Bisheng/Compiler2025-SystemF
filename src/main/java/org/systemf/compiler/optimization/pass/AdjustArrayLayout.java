package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.IndexVarianceAnalysisResult;
import org.systemf.compiler.analysis.PointerAnalysisResult;
import org.systemf.compiler.analysis.util.BelongingHelper;
import org.systemf.compiler.ir.AllocationSite;
import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.type.Array;
import org.systemf.compiler.ir.type.interfaces.Atom;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.constant.ArrayZeroInitializer;
import org.systemf.compiler.ir.value.constant.Constant;
import org.systemf.compiler.ir.value.constant.ConstantArray;
import org.systemf.compiler.ir.value.instruction.Instruction;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Alloca;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.GetPtr;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.optimization.pass.util.CodeGenHelper;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.Pair;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Depend on: PointerAnalysis, IndexVarianceAnalysis
 * <p>
 * Applicable to: IR
 */
public enum AdjustArrayLayout implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		return new AdjustArrayLayoutContext(module).run();
	}

	private static class AdjustArrayLayoutContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private final PointerAnalysisResult ptrResult;
		private final IndexVarianceAnalysisResult variance;
		private final Map<Instruction, BasicBlock> belonging;
		private IRBuilder builder;
		private List<Integer> idealIndex;
		private List<Array> newTypes;
		private int[] transformTmp;

		public AdjustArrayLayoutContext(Module module) {
			this.module = module;
			this.ptrResult = query.getAttribute(module, PointerAnalysisResult.class);
			this.variance = query.getAttribute(module, IndexVarianceAnalysisResult.class);
			this.belonging = BelongingHelper.getBlockBelonging(module);
		}

		private boolean constructIdealIndex(AllocationSite allocSite) {
			var thisVar = variance.variance(allocSite);
			transformTmp = new int[thisVar.size()];
			var index = IntStream.range(0, thisVar.size()).boxed().toList();
			idealIndex = new ArrayList<>(index);
			idealIndex.sort(Comparator.comparingInt(thisVar::get));
			return !index.equals(idealIndex);
		}

		private boolean collectIndexChain(Value value, List<Value> out) {
			if (value instanceof AllocationSite) return true;
			if (value instanceof GetPtr getPtr) {
				out.add(getPtr.getIndex());
				return collectIndexChain(getPtr.getArrayPtr(), out);
			}
			return false;
		}

		private void constructNewType(Sized oldType) {
			var lengths = new ArrayList<Integer>();
			while (!(oldType instanceof Atom)) {
				if (oldType instanceof Array arr) {
					lengths.add(arr.length);
					oldType = arr.getElementType();
				} else throw new IllegalArgumentException();
			}
			var newType = oldType;
			newTypes = new ArrayList<>();
			for (var i : idealIndex.reversed()) {
				int l = lengths.get(i);
				var newArray = builder.buildArrayType(newType, l);
				newType = newArray;
				newTypes.add(newArray);
			}
			Collections.reverse(newTypes);
		}

		private List<Value> collectIndexChain(Value value) {
			var res = new ArrayList<Value>();
			if (collectIndexChain(value, res)) {
				Collections.reverse(res);
				return res;
			} else return null;
		}

		private void renewIndexChain(AllocationSite allocSite, Instruction inst, Value oldPtr, List<Value> chain) {
			var block = belonging.get(inst);
			builder.setPosition(CodeGenHelper.skipAllPhis(block));
			Value last = allocSite;
			for (var i : idealIndex) last = builder.buildGetPtr(last, chain.get(i), "renewIndex");
			inst.replaceAll(oldPtr, last);
		}

		private Constant transformArray(int cur, Constant arr) {
			if (cur >= idealIndex.size()) {
				for (int index : transformTmp) arr = ((ConstantArray) arr).getContent(index);
				return arr;
			}

			var curArray = newTypes.get(cur);
			var dimLen = curArray.length;
			var content = new Constant[dimLen];
			for (int i = 0; i < dimLen; ++i) {
				transformTmp[idealIndex.get(cur)] = i;
				content[i] = transformArray(cur + 1, arr);
			}
			return builder.buildConstantArray(curArray.getElementType(), content);
		}

		private Constant renewConstant(Constant constant) {
			if (constant instanceof ArrayZeroInitializer)
				return builder.buildConstantArray(newTypes.getFirst().getElementType(), newTypes.getFirst().length);
			else if (constant instanceof ConstantArray arr) return transformArray(0, arr);
			else throw new IllegalArgumentException();
		}

		private boolean processAllocSiteCommon(AllocationSite allocSite) {
			if (!constructIdealIndex(allocSite)) return false;
			constructNewType(allocSite.valueType());
			var depInst = ptrResult.pointedBy(allocSite).stream().flatMap(ptr -> ptr.getDependant().stream()).distinct()
					.toList();
			var accessIndex = new LinkedHashMap<Instruction, Pair<Value, List<Value>>>();
			var largeStore = new ArrayList<Store>();
			for (var inst : depInst)
				if (inst instanceof Load load) {
					var ptr = load.getPointer();
					var chain = collectIndexChain(ptr);
					if (chain == null) return false;
					accessIndex.put(load, Pair.of(ptr, chain));
				} else if (inst instanceof Store store) {
					var dest = store.getDest();
					if (!(store.getSrc().getType() instanceof Atom)) {
						if (dest != allocSite) return false;
						largeStore.add(store);
					} else {
						var chain = collectIndexChain(dest);
						if (chain == null) return false;
						accessIndex.put(store, Pair.of(dest, chain));
					}
				}
			accessIndex.forEach((inst, pair) -> renewIndexChain(allocSite, inst, pair.left(), pair.right()));
			largeStore.forEach(store -> store.setSrc(renewConstant((Constant) store.getSrc())));
			return true;
		}

		private boolean processAlloca(Alloca alloca) {
			if (!processAllocSiteCommon(alloca)) return false;
			builder.setPosition(CodeGenHelper.skipAllPhis(belonging.get(alloca)));
			var newAlloca = builder.buildAlloca(newTypes.getFirst(), alloca.getName());
			alloca.replaceAllUsage(newAlloca);
			return true;
		}

		private boolean processGlobal(GlobalVariable global) {
			if (!processAllocSiteCommon(global)) return false;
			var newGlobal = builder.buildGlobalVariable(module.getNonConflictName(global.getName()),
					newTypes.getFirst(),
					renewConstant(global.getInitializer()));
			global.replaceAllUsage(newGlobal);
			return true;
		}

		private boolean processAllocSite(AllocationSite allocSite) {
			return switch (allocSite) {
				case Alloca alloca -> processAlloca(alloca);
				case GlobalVariable global -> processGlobal(global);
				default -> false;
			};
		}

		public boolean run() {
			try (var builder = new IRBuilder(module)) {
				this.builder = builder;
				var res = variance.variance().keySet().stream().map(this::processAllocSite)
						.reduce(false, (a, b) -> a || b);
				if (res) {
					module.getFunctions().values().forEach(query::invalidateAllAttributes);
					query.invalidateAllAttributes(module);
				}
				return res;
			}
		}
	}
}
