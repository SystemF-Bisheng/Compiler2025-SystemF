package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.type.Pointer;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.Call;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.CallVoid;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Alloca;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.GetPtr;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.ir.value.instruction.nonterminal.miscellaneous.Phi;
import org.systemf.compiler.ir.value.instruction.terminal.Ret;
import org.systemf.compiler.query.AttributeProvider;
import org.systemf.compiler.util.GraphClosure;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Interprocedural pointer analysis, inclusion based
 */
public enum PointerAnalysis implements AttributeProvider<Module, PointerAnalysisResult> {
	INSTANCE;

	@Override
	public PointerAnalysisResult getAttribute(Module entity) {
		var res = new PointerAnalysisResult(new HashMap<>(), new HashMap<>());

		var context = new PointerAnalysisContext();
		context.collectAllocationSite(entity);
		entity.getFunctions().values().forEach(context::analyzeFunction);
		context.collectResult(res);

		return res;
	}

	private static class PointerAnalysisContext {
		private final GraphClosure<Node, Value> closure = new GraphClosure<>();
		private final HashMap<Value, Node> allocSiteInner = new HashMap<>();
		private final HashMap<Value, Node> valuePoint = new HashMap<>();
		private final HashMap<Function, Node> functionReturn = new HashMap<>();

		private void collectFunctionAllocationSite(Function function) {
			for (var bb : function.getBlocks())
				for (var inst : bb.instructions)
					if (inst instanceof Alloca alloc) {
						allocSiteInner.put(alloc, new Node());
						var valueNode = new Node();
						valuePoint.put(alloc, valueNode);
						closure.addData(valueNode, alloc);
					}
			if (function.getReturnType() instanceof Pointer) functionReturn.put(function, new Node());
		}

		public void collectAllocationSite(Module module) {
			module.getGlobalDeclarations().values().forEach(global -> {
				allocSiteInner.put(global, new Node());
				var valueNode = new Node();
				valuePoint.put(global, valueNode);
				closure.addData(valueNode, global);
			});
			module.getFunctions().values().forEach(this::collectFunctionAllocationSite);
		}

		private Node nodeOfInner(Value v) {
			return allocSiteInner.computeIfAbsent(v, _ -> new Node());
		}

		private Node nodeOfValue(Value v) {
			return valuePoint.computeIfAbsent(v, _ -> new Node());
		}

		private void fromValueToValue(Value a, Value b) {
			var aNode = nodeOfValue(a);
			var bNode = nodeOfValue(b);
			closure.addEdge(aNode, bNode);
		}

		private void fromPtrToValue(Value ptr, Value value) {
			var valueNode = nodeOfValue(value);
			var ptrNode = nodeOfValue(ptr);
			closure.addReaction(ptrNode, v -> {
				var vNode = nodeOfInner(v);
				closure.addEdge(vNode, valueNode);
			});
		}

		private void fromValueToPtr(Value value, Value ptr) {
			var valueNode = nodeOfValue(value);
			var ptrNode = nodeOfValue(ptr);
			closure.addReaction(ptrNode, v -> {
				var vNode = nodeOfInner(v);
				closure.addEdge(valueNode, vNode);
			});
		}

		private void handleAbstractCall(AbstractCall abstractCall) {
			if (!(abstractCall.getFunction() instanceof Function func)) return;
			var args = abstractCall.getArgs();
			var params = func.getFormalArgs();
			for (int i = 0; i < args.length; ++i) {
				var arg = args[i];
				if (!(arg.getType() instanceof Pointer)) continue;
				fromValueToValue(arg, params[i]);
			}
		}

		public void analyzeFunction(Function function) {
			for (var bb : function.getBlocks())
				for (var inst : bb.instructions)
					if (inst instanceof GetPtr getPtr) fromValueToValue(getPtr.getArrayPtr(), getPtr);
					else if (inst instanceof Phi phi)
						phi.getIncoming().values().forEach(in -> fromValueToValue(in, phi));
					else if (inst instanceof Load load) fromPtrToValue(load.getPointer(), load);
					else if (inst instanceof Store store) fromValueToPtr(store.getSrc(), store.getDest());
					else if (inst instanceof Call call) {
						handleAbstractCall(call);
						if (call.getFunction() instanceof Function func) if (functionReturn.containsKey(func))
							closure.addEdge(functionReturn.get(func), nodeOfValue(call));
					} else if (inst instanceof CallVoid call) handleAbstractCall(call);
					else if (inst instanceof Ret ret) {
						if (functionReturn.containsKey(function))
							closure.addEdge(nodeOfValue(ret.getReturnValue()), functionReturn.get(function));
					}
		}

		public void collectResult(PointerAnalysisResult out) {
			var data = closure.getData();
			valuePoint.forEach((value, node) -> {
				var ptrTo = data.get(node);
				if (ptrTo == null || ptrTo.isEmpty()) return;
				out.pointTo().put(value, ptrTo);
				for (var alloca : ptrTo) {
					var pointed = out.pointed();
					pointed.computeIfAbsent(alloca, _ -> new HashSet<>()).add(value);
				}
			});
		}

		private static class Node {
		}
	}
}