package org.systemf.compiler.optimization.pass;

import org.systemf.compiler.analysis.CFGAnalysisResult;
import org.systemf.compiler.analysis.DominanceAnalysisResult;
import org.systemf.compiler.analysis.PointerAnalysisResult;
import org.systemf.compiler.ir.IRBuilder;
import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.block.BasicBlock;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.type.interfaces.Atom;
import org.systemf.compiler.ir.type.interfaces.Sized;
import org.systemf.compiler.ir.value.Parameter;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Alloca;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.query.QueryManager;
import org.systemf.compiler.util.Tree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public enum MemToReg implements OptPass {
	INSTANCE;

	@Override
	public boolean run(Module module) {
		var context = new MemToRegContext(module);
		return context.run();
	}

	private static class MemToRegContext {
		private final QueryManager query = QueryManager.getInstance();
		private final Module module;
		private final PointerAnalysisResult pointerResult;
		private IRBuilder builder;
		private Alloca curAlloc;
		private Sized allocatedType;
		private Set<Value> allocPointedBy;
		private Tree<BasicBlock> domTree;
		private HashMap<BasicBlock, Value> curProvide;
		private HashMap<BasicBlock, Value> postProvide;

		public MemToRegContext(Module module) {
			this.module = module;
			this.pointerResult = query.getAttribute(module, PointerAnalysisResult.class);
		}

		private boolean checkLocal(Alloca alloc) {
			return pointerResult.pointedBy(alloc).stream().noneMatch(ptr -> ptr instanceof Parameter);
		}

		private boolean checkUnambiguity(Alloca alloc) {
			return pointerResult.pointedBy(alloc).stream().allMatch(ptr -> pointerResult.pointTo(ptr).size() == 1);
		}

		private Value inlineInBlock(BasicBlock block) {
			Store lastStore = null;
			for (var iter = block.instructions.listIterator(); iter.hasNext(); ) {
				var inst = iter.next();
				if (inst instanceof Store store) {
					if (!allocPointedBy.contains(store.getDest())) continue;
					lastStore = store;
				} else if (inst instanceof Load load) {
					if (!allocPointedBy.contains(load.getPointer())) continue;
					if (lastStore == null) continue;
					load.replaceAllUsage(lastStore.getSrc());
					load.unregister();
					iter.remove();
				} else if (inst == curAlloc) iter.remove();
			}
			if (lastStore != null) {
				var value = lastStore.getSrc();
				for (var iter = block.instructions.listIterator(); iter.hasNext(); ) {
					var inst = iter.next();
					if (!(inst instanceof Store store)) continue;
					if (!allocPointedBy.contains(store.getDest())) continue;
					inst.unregister();
					iter.remove();
				}
				return value;
			}
			return null;
		}

		private void dfs(BasicBlock cur, Value pass) {
			if (pass != null) {
				curProvide.putIfAbsent(cur, pass);
				postProvide.putIfAbsent(cur, pass);
			}

			for (var iter = cur.instructions.listIterator(); iter.hasNext(); ) {
				if (!(iter.next() instanceof Load load)) continue;
				if (!allocPointedBy.contains(load.getPointer())) continue;
				var curValue = Objects.requireNonNullElseGet(curProvide.get(cur),
						() -> builder.buildUndefined(allocatedType));
				load.replaceAllUsage(curValue);
				if (postProvide.get(cur) == load) postProvide.put(cur, curValue);
				load.unregister();
				iter.remove();
			}

			var nxtPass = postProvide.get(cur);
			for (var nxt : domTree.getChildren(cur)) dfs(nxt, nxtPass);
		}

		private void inline(Function function, Alloca alloc) {
			var dominance = query.getAttribute(function, DominanceAnalysisResult.class);
			this.curAlloc = alloc;
			this.allocatedType = alloc.valueType;
			this.allocPointedBy = pointerResult.pointedBy(alloc);
			this.domTree = dominance.dominance();
			this.curProvide = new HashMap<>();
			this.postProvide = new HashMap<>();

			var iteration = new HashSet<BasicBlock>();
			for (var block : function.getBlocks()) {
				var store = inlineInBlock(block);
				if (store != null) {
					postProvide.put(block, store);
					iteration.add(block);
				}
			}
			var inserted = new HashMap<BasicBlock, Phi>();

			while (!iteration.isEmpty()) {
				var frontier = new HashSet<BasicBlock>();
				for (var block : iteration) {
					var df = dominance.dominanceFrontier(block);
					for (var toInsert : df) {
						if (inserted.containsKey(toInsert)) continue;
						frontier.add(toInsert);
					}
				}
				for (var block : frontier) {
					builder.setPosition(block.instructions.listIterator());
					var phi = builder.buildPhi(alloc.valueType, alloc.getName());
					inserted.put(block, phi);
					curProvide.put(block, phi);
					postProvide.putIfAbsent(block, phi);
				}
				iteration = frontier;
			}

			dfs(domTree.getRoot(), null);

			var cfg = query.getAttribute(function, CFGAnalysisResult.class);
			inserted.forEach((block, phi) -> {
				for (var pred : cfg.predecessors(block))
					phi.addIncoming(pred, Objects.requireNonNullElseGet(postProvide.get(pred),
							() -> builder.buildUndefined(allocatedType)));
			});

			alloc.unregister();
		}

		private boolean processFunction(Function function) {
			boolean flag = false;

			var toReg = function.allInstructions().filter(inst -> inst instanceof Alloca).map(inst -> (Alloca) inst)
					.filter(alloc -> alloc.valueType instanceof Atom).filter(this::checkLocal)
					.filter(this::checkUnambiguity).toList();
			if (!toReg.isEmpty()) flag = true;
			toReg.forEach(alloc -> inline(function, alloc));

			if (flag) query.invalidateAllAttributes(function);
			return flag;
		}

		public boolean run() {
			builder = new IRBuilder(module);
			var res = module.getFunctions().values().stream().map(this::processFunction)
					.reduce(false, (a, b) -> a || b);
			if (res) query.invalidateAllAttributes(module);
			builder.close();
			builder = null;
			return res;
		}
	}
}