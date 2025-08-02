package org.systemf.compiler.lower.rv64gc.analysis;

import org.systemf.compiler.ir.global.Function;
import org.systemf.compiler.ir.value.Value;
import org.systemf.compiler.lower.rv64gc.module.RVModule;
import org.systemf.compiler.lower.rv64gc.module.position.RVRegister;
import org.systemf.compiler.query.AttributeProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum RVRegUsageAnalysis implements AttributeProvider<RVModule, RVRegUsageAnalysisResult> {
	INSTANCE;

	private void collect(RVModule rvModule, Function function, Map<Function, Set<RVRegister>> usage) {
		var curUsage = new HashSet<RVRegister>();
		for (var param : function.getFormalArgs())
			if (rvModule.position().get(param) instanceof RVRegister reg) curUsage.add(reg);
		function.allInstructions().filter(inst -> inst instanceof Value).map(inst -> rvModule.position().get(inst))
				.filter(pos -> pos instanceof RVRegister).forEach(reg -> curUsage.add((RVRegister) reg));
		usage.put(function, curUsage);
	}

	@Override
	public RVRegUsageAnalysisResult getAttribute(RVModule entity) {
		var res = new HashMap<Function, Set<RVRegister>>();
		entity.module().getFunctions().values().forEach(func -> collect(entity, func, res));
		return new RVRegUsageAnalysisResult(res);
	}
}
