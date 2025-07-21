package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Alloca;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Store;
import org.systemf.compiler.query.AttributeProvider;
import org.systemf.compiler.query.QueryManager;

import java.util.HashSet;
import java.util.stream.Collectors;

public enum FunctionSideEffectAnalysis implements AttributeProvider<Module, FunctionSideEffectResult> {
	INSTANCE;

	private boolean memoryAnalysis(PointerAnalysisResult ptrResult, Function function) {
		var localAlloc = function.allInstructions().filter(inst -> inst instanceof Alloca).map(inst -> (Value) inst)
				.collect(Collectors.toSet());
		return !function.allInstructions().filter(inst -> inst instanceof Store).map(inst -> (Store) inst)
				.allMatch(store -> localAlloc.containsAll(ptrResult.pointTo(store.getDest())));
	}

	private boolean propagateAnalysis(FunctionSideEffectResult result, Function function) {
		return function.allInstructions().filter(inst -> inst instanceof AbstractCall).map(inst -> (AbstractCall) inst)
				.map(AbstractCall::getFunction).anyMatch(func -> {
					if (!(func instanceof Function userFunc)) return true;
					return result.sideEffect(userFunc);
				});
	}

	@Override
	public FunctionSideEffectResult getAttribute(Module entity) {
		var ptrResult = QueryManager.getInstance().getAttribute(entity, PointerAnalysisResult.class);
		var res = new FunctionSideEffectResult(new HashSet<>());
		var functions = entity.getFunctions().values();
		var sideEffects = res.sideEffectFunctions();
		for (var func : functions)
			if (memoryAnalysis(ptrResult, func)) sideEffects.add(func);
		while (true) {
			var flag = true;
			for (var func : functions) {
				if (sideEffects.contains(func)) continue;
				if (propagateAnalysis(res, func)) {
					sideEffects.add(func);
					flag = false;
				}
			}
			if (flag) break;
		}

		return res;
	}
}
