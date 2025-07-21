package org.systemf.compiler.analysis;

import org.systemf.compiler.ir.Module;
import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.global.GlobalVariable;
import org.systemf.compiler.ir.type.interfaces.Atom;
import org.systemf.compiler.ir.value.DummyValue;
import org.systemf.compiler.ir.value.instruction.nonterminal.invoke.AbstractCall;
import org.systemf.compiler.ir.value.instruction.nonterminal.memory.Load;
import org.systemf.compiler.query.AttributeProvider;
import org.systemf.compiler.query.QueryManager;

import java.util.Arrays;
import java.util.HashSet;

public enum FunctionRepeatableAnalysis implements AttributeProvider<Module, FunctionRepeatableResult> {
	INSTANCE;

	private boolean memoryAnalysis(PointerAnalysisResult ptrResult, Function function) {
		if (!Arrays.stream(function.getFormalArgs()).map(DummyValue::getType).allMatch(type -> type instanceof Atom))
			return true;

		return function.allInstructions().filter(inst -> inst instanceof Load).map(inst -> (Load) inst).allMatch(
				load -> ptrResult.pointTo(load.getPointer()).stream()
						.anyMatch(alloc -> alloc instanceof GlobalVariable));
	}

	private boolean propagateAnalysis(FunctionRepeatableResult result, Function function) {
		return function.allInstructions().filter(inst -> inst instanceof AbstractCall).map(inst -> (AbstractCall) inst)
				.map(AbstractCall::getFunction).anyMatch(func -> {
					if (!(func instanceof Function userFunc)) return true;
					return result.nonRepeatable(userFunc);
				});
	}

	@Override
	public FunctionRepeatableResult getAttribute(Module entity) {
		var ptrResult = QueryManager.getInstance().getAttribute(entity, PointerAnalysisResult.class);
		var res = new FunctionRepeatableResult(new HashSet<>());
		var nonRepeatable = res.nonRepeatableFunctions();
		var functions = entity.getFunctions().values();
		for (var func : functions)
			if (memoryAnalysis(ptrResult, func)) nonRepeatable.add(func);
		while (true) {
			var flag = true;
			for (var func : functions) {
				if (nonRepeatable.contains(func)) continue;
				if (propagateAnalysis(res, func)) {
					nonRepeatable.add(func);
					flag = false;
				}
			}
			if (flag) break;
		}
		return res;
	}
}
